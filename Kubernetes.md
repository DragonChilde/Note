# `Kubeadm`创建集群

> 请参照以前Docker安装。先提前为所有机器安装Docker

## 安装`kubeadm`

- 一台兼容的 Linux 主机。Kubernetes 项目为基于 Debian 和 Red Hat 的 Linux 发行版以及一些不提供包管理器的发行版提供通用的指令
- 每台机器 2 GB 或更多的 RAM （如果少于这个数字将会影响你应用的运行内存)

- 2 CPU 核或更多
- 集群中的所有机器的网络彼此均能相互连接(公网和内网都可以)

- - **设置防火墙放行规则**

- 节点之中不可以有重复的主机名、MAC 地址或 product_uuid。请参见[这里](https://kubernetes.io/zh/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#verify-mac-address)了解更多详细信息。

- - **设置不同hostname**

- 开启机器上的某些端口。请参见[这里](https://kubernetes.io/zh/docs/setup/production-environment/tools/kubeadm/install-kubeadm/#check-required-ports) 了解更多详细信息。

- - **内网互信**

- 禁用交换分区。为了保证 kubelet 正常工作，你 **必须** 禁用交换分区。

- - **永久关闭**

### 基础环境

> 所有机器执行以下操作

```sh
#各个机器设置自己的域名
hostnamectl set-hostname xxxx

# 将 SELinux 设置为 permissive 模式（相当于将其禁用）
sudo setenforce 0
sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config

#关闭swap
swapoff -a  
sed -ri 's/.*swap.*/#&/' /etc/fstab

#禁用防火墙firewalld
systemctl stop firewalld
systemctl disable firewalld

#上面三步非常关键，如果防火墙没有关闭，会有各种各样的问题，导致机器中的节点无法正常通信（别问我是怎么知道的~）

#允许 iptables 检查桥接流量
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
br_netfilter
EOF

cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sudo sysctl --system
```

### 安装`kubelet`、`kubeadm`、`kubectl`

```sh
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
   http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
exclude=kubelet kubeadm kubectl
EOF


sudo yum install -y kubelet-1.20.9 kubeadm-1.20.9 kubectl-1.20.9 --disableexcludes=kubernetes

sudo systemctl enable --now kubelet
```

### 使用`kubeadm`引导集群

1. 下载各个机器需要的镜像（可选 ，不用下载指定版本，下面`kubeadm init`初始化时会自适应一并下载）

   ```sh
   sudo tee ./images.sh <<-'EOF'
   #!/bin/bash
   images=(
   kube-apiserver:v1.20.9
   kube-proxy:v1.20.9
   kube-controller-manager:v1.20.9
   kube-scheduler:v1.20.9
   coredns:1.7.0
   etcd:3.4.13-0
   pause:3.2
   )
   for imageName in ${images[@]} ; do
   docker pull registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/$imageName
   done
   EOF
      
   chmod +x ./images.sh && ./images.sh
   ```

2. 初始化主节点

   ```sh
   #所有机器添加master域名映射，以下ip需要修改为自己的，通过ipconfig获取master的，如下
   echo "172.16.0.117  master" >> /etc/hosts
   
   [root@iZ7xv6wlrgwsz84rwpu5r9Z ~]# ifconfig
   docker0: flags=4099<UP,BROADCAST,MULTICAST>  mtu 1500
           inet 172.17.0.1  netmask 255.255.0.0  broadcast 172.17.255.255
           ether 02:42:ae:2c:0a:fa  txqueuelen 0  (Ethernet)
           RX packets 0  bytes 0 (0.0 B)
           RX errors 0  dropped 0  overruns 0  frame 0
           TX packets 0  bytes 0 (0.0 B)
           TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
   
   eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
           inet 172.16.0.117  netmask 255.255.255.0  broadcast 172.16.0.255
           inet6 fe80::216:3eff:fe01:58fa  prefixlen 64  scopeid 0x20<link>
           ether 00:16:3e:01:58:fa  txqueuelen 1000  (Ethernet)
           RX packets 136251  bytes 200834791 (191.5 MiB)
           RX errors 0  dropped 0  overruns 0  frame 0
           TX packets 20888  bytes 2147468 (2.0 MiB)
           TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
   
   lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536
           inet 127.0.0.1  netmask 255.0.0.0
           inet6 ::1  prefixlen 128  scopeid 0x10<host>
           loop  txqueuelen 1000  (Local Loopback)
           RX packets 0  bytes 0 (0.0 B)
           RX errors 0  dropped 0  overruns 0  frame 0
           TX packets 0  bytes 0 (0.0 B)
           TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
   
   
   #主节点初始化(注意，下面只在主节点执行)
   kubeadm init \
   --apiserver-advertise-address=172.16.0.117 \
   #--control-plane-endpoint=master \	#高可用配置负载均衡IP地址或DNS名称
   --image-repository registry.aliyuncs.com/google_containers \
   --kubernetes-version v1.20.9 \
   --service-cidr=10.96.0.0/16 \
   --pod-network-cidr=192.168.0.0/16
   
   #所有网络范围不重叠
   ```

   > 这里使用的VM虚拟机进行安装，线上会略有区别

   > 说明：
   >
   > - `--apiserver-advertise-address`：**集群通告地址，就是Master节点的IP地址；**
   > - `--image-repository` ：**由于默认拉取镜像地址k8s.gcr.io国内无法访问，这里指定阿里云镜像仓库地址；**
   > - `--kubernetes-version`：**K8s版本，与上面安装的一致；**
   > - `--service-cidr`：**集群内部虚拟网络，Pod统一访问入口；**
   > - `--pod-network-cidr`：**Pod网络，与下面部署的CNI网络组件yaml中保持一致；**
   >
   > **注：集群内部虚拟地址和Pod网络地址可自行指定，但是必须要和下面的配置要保持一致！**

   > **异常**
   >
   > ```
   > /proc/sys/net/ipv4/ip_forward contents are not set to 1
   > ```
   >
   > **解决**
   >
   > ```sh
   > echo 1 > /proc/sys/net/ipv4/ip_forward
   > ```

3. `master`成功初始化显示如下,把成功提示`copy`出来，到时会用到下面命令提示

   ```sh
   Your Kubernetes control-plane has initialized successfully!
   
   To start using your cluster, you need to run the following as a regular user:
   
     mkdir -p $HOME/.kube
     sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
     sudo chown $(id -u):$(id -g) $HOME/.kube/config
   
   Alternatively, if you are the root user, you can run:
   
     export KUBECONFIG=/etc/kubernetes/admin.conf
   
   You should now deploy a pod network to the cluster.
   Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
     https://kubernetes.io/docs/concepts/cluster-administration/addons/
   
   Then you can join any number of worker nodes by running the following on each as root:
   
   kubeadm join 172.16.0.117:6443 --token qdv58l.qs5qug56i7i3lqn8 \
       --discovery-token-ca-cert-hash sha256:e71425503b48d306c5772933458a725d392c0afdf6837ee34ebd45963b538200 
   ```
   
4. 设置`.kube/config`，根据上面成功提示，拷贝`kubectl`使用的连接`k8s`认证文件到默认路径

   ```sh
     mkdir -p $HOME/.kube
     sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
     sudo chown $(id -u):$(id -g) $HOME/.kube/config
   ```

5. 此时查看Master节点的状态：

   ```sh
   #查看集群所有节点
   kubectl get nodes
   
   NAME         STATUS     ROLES                  AGE   VERSION
   k8s-master   NotReady   control-plane,master   10m   v1.20.9
   
   #根据配置文件，给集群创建资源
   kubectl apply -f xxxx.yaml
   
   #查看集群部署了哪些应用？
   docker ps   ===   kubectl get pods -A
   # 运行中的应用在docker里面叫容器，在k8s里面叫Pod
   kubectl get pods -A
   ```
   
6. 安装`Calico`插件
   
   `CNI`是`Kubernetes`中的一个调用网络实现的接口标准；
   
   `Kubelet `通过这个标准的 API 来调用不同的网络插件以实现不同的网络配置方式，实现了这个接口的就是 CNI 插件，它实现了一系列的 CNI API 接口；
   
   常用的CNI插件有很多，比如：
   
   - Flannel；
   - Calico；
   - Canal；
   - Weave；
   - ……
   
   这里我们选用的是`Calico`；
   
   > **关于Calico：**
   >
   > **Calico是一个纯三层的数据中心网络方案，Calico支持广泛的平台，包括Kubernetes、OpenStack等；**
   >
   > **Calico 在每一个计算节点利用 Linux Kernel 实现了一个高效的虚拟路由器（ vRouter） 来负责数据转发，而每个 vRouter 通过 BGP 协议负责把自己上运行的 workload 的路由信息向整个 Calico 网络内传播；**
   >
   > 官方文档：
   >
   > - https://docs.projectcalico.org/getting-started/kubernetes/quickstart
   >
   >   相关阅读：
   >
   > - [CNI容器网络介绍](https://kubernetes.io/docs/concepts/cluster-administration/networking/#how-to-implement-the-kubernetes-networking-model)
   >
   > - [从零开始入门 K8s | 理解 CNI 和 CNI 插件](https://www.kubernetes.org.cn/6908.html)
   >
   > - [CNI - Container Network Interface（容器网络接口）](https://jimmysong.io/kubernetes-handbook/concepts/cni.html)
   >
   > - [Comparing Kubernetes CNI Providers: Flannel, Calico, Canal, and Weave](https://rancher.com/blog/2019/2019-03-21-comparing-kubernetes-cni-providers-flannel-calico-canal-and-weave/)
   
   首先，通过wget下载Calico配置文件：
   
   ```sh
   curl https://docs.projectcalico.org/manifests/calico.yaml -O
   ```
   
   （**可选，如内网段非默认内网段范围，可不用修改**）随后修改配置文件中的`CALICO_IPV4POOL_CIDR`：
   
   > **修改Pod网络（CALICO_IPV4POOL_CIDR），与前面kubeadm init指定的一样**
   
   ```yaml
   # 
   vim calico.yaml 
   
   # The default IPv4 pool to create on startup if none exists. Pod IPs will be
   # chosen from this range. Changing this value after installation will have
   # no effect. This should fall within `--cluster-cidr`.
   -# - name: CALICO_IPV4POOL_CIDR
   -#   value: "192.168.0.0/16"
   + - name: CALICO_IPV4POOL_CIDR
   +   value: "10.244.0.0/16"
   # Disable file logging so `kubectl logs` works.
   ```
   
   > 注意，这里有个BUG,必须把注释后的缩进删除上下文对齐，否则会执行下面的命令报`error: error parsing calico.yaml: error converting YAML to JSON: yaml: line 182: did not find expected '-' indicator`
   
   最后通过配置文件启动服务：
   
   ```sh
   kubectl apply -f calico.yaml
   ```
   
   等待一段时间后(安装期间等待有10多分钟，时间比较长)，查看`pod`状态，全部都已经是`Running`状态：
   
   ```sh
   [root@k8s-master ~]# kubectl get pods -n kube-system
   NAME                                       READY   STATUS    RESTARTS   AGE
   calico-kube-controllers-756dd4db79-vv5mg   1/1     Running   0          3m37s
   calico-node-d269w                          1/1     Running   0          3m37s
   coredns-7f89b7bc75-dqqkw                   1/1     Running   0          5m25s
   coredns-7f89b7bc75-h7bpw                   1/1     Running   0          5m25s
   etcd-k8s-master                            1/1     Running   0          5m38s
   kube-apiserver-k8s-master                  1/1     Running   0          5m38s
   kube-controller-manager-k8s-master         1/1     Running   0          5m38s
   kube-proxy-kqjnp                           1/1     Running   0          5m26s
   kube-scheduler-k8s-master                  1/1     Running   0          5m38s
   ```
   
   可以看到，所有的服务都已经`Running`；同时查看节点状态：
   
   ```sh
   [root@iZ7xv6wlrgwsz84rwpu5r9Z ~]# kubectl get nodes
   NAME     STATUS   ROLES                  AGE   VERSION
   master   Ready    control-plane,master   11m   v1.20.9
   ```
   
   此时`Master`节点已经变为了`Ready`状态！
   
   > **注：在将Node节点加入Master之前必须先安装CNI（即使不是Calico）；**
   >
   > **否则有可能出现子节点无法连接Master的情况；**
   
8. `Node`节点加入`Master`
   在`Node`节点中运行之前在`Master`节点初始化后`kubeadm init`输出的`kubeadm join`命令：
   
   ```sh
   kubeadm join 172.16.0.117:6443 --token qdv58l.qs5qug56i7i3lqn8 \
       --discovery-token-ca-cert-hash sha256:e71425503b48d306c5772933458a725d392c0afdf6837ee34ebd45963b538200 
   ```
   
   等待片刻，`Node`节点即加入至Master中；
   
   **集群创建完毕！**
   
   > **注：默认token有效期为24小时，当过期之后，该token就不可用了；**
   >
   > 这时就需要重新创建token，操作如下：
   >
   > ```sh
   >   kubeadm token create --print-join-command
   > ```
   >
   > 通过该命令可以快捷生成token；
   
   等待所有`Node`节点成功加入到`Master`,在`Master`查看如下
   
   ```sh
   [root@iZ7xv6wlrgwsz84rwpu5r9Z ~]# kubectl get pods -n kube-system
   NAME                                       READY   STATUS    RESTARTS   AGE
   calico-kube-controllers-756dd4db79-r2cvd   1/1     Running   0          6m47s
   calico-node-kdhzf                          1/1     Running   0          2m42s
   calico-node-pd92w                          1/1     Running   0          2m54s
   calico-node-snf2f                          1/1     Running   0          6m48s
   coredns-7f89b7bc75-b6chr                   1/1     Running   0          14m
   coredns-7f89b7bc75-gdxwp                   1/1     Running   0          14m
   etcd-master                                1/1     Running   0          14m
   kube-apiserver-master                      1/1     Running   0          14m
   kube-controller-manager-master             1/1     Running   0          14m
   kube-proxy-578n2                           1/1     Running   0          2m42s
   kube-proxy-82j8p                           1/1     Running   0          14m
   kube-proxy-lfn94                           1/1     Running   0          2m54s
   kube-scheduler-master                      1/1     Running   0          14m
   [root@iZ7xv6wlrgwsz84rwpu5r9Z ~]# kubectl get nodes
   NAME     STATUS     ROLES                  AGE   VERSION
   master   Ready      control-plane,master   12m   v1.20.9
   node1    NotReady   <none>                 14s   v1.20.9
   node2    NotReady   <none>                 2s    v1.20.9
   ```

### 部署`dashboard`

接下来为Kubernetes创建后台管理面板，方便查看和管理；

> Dashboard的网址：
>
> - https://github.com/kubernetes/dashboard/
>
> **使用时需要根据kubenetes版本选择Dashboard版本，此处为v2.3.1；**

#### 下载并部署

首先通过wget获取配置文件：

```sh
wget https://raw.githubusercontent.com/kubernetes/dashboard/v2.3.1/aio/deploy/recommended.yaml -O dashboard.yaml
```

> **由于在默认情况下，Dashboard只能集群内部访问；因此，需要修改Service为NodePort类型，暴露到外部；**

文件修改内容如下：

```sh
vim dashboard.yaml

kind: Service
apiVersion: v1
metadata:
  labels:
    k8s-app: kubernetes-dashboard
  name: kubernetes-dashboard
  namespace: kubernetes-dashboard
spec:
+ type: NodePort
  ports:
    - port: 443
      targetPort: 8443
+     nodePort: 30001
  selector:
    k8s-app: kubernetes-dashboard
```

随后，将配置文件应用：

```sh
kubectl apply -f dashboard.yaml
```

等待服务部署后查看：

```sh
[root@k8s-master ~]# kubectl get pods -n kubernetes-dashboard
NAME                                         READY   STATUS    RESTARTS   AGE
dashboard-metrics-scraper-79c5968bdc-9sdpv   1/1     Running   0          30s
kubernetes-dashboard-658485d5c7-xq6rq        1/1     Running   0          30s
```

查看放行端口号

```
[root@iZ7xv6wlrgwsz84rwpu5r9Z ~]# kubectl get svc -A |grep kubernetes-dashboard
kubernetes-dashboard   dashboard-metrics-scraper   ClusterIP   10.96.177.150   <none>        8000/TCP                 47s
kubernetes-dashboard   kubernetes-dashboard        NodePort    10.96.74.65     <none>        443:32599/TCP            47s
```

#### 创建用户角色

1. 创建访问账号，准备一个yaml文件；

   ```yaml
   apiVersion: v1
   kind: ServiceAccount
   metadata:
     name: admin-user
     namespace: kubernetes-dashboard
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: ClusterRoleBinding
   metadata:
     name: admin-user
   roleRef:
     apiGroup: rbac.authorization.k8s.io
     kind: ClusterRole
     name: cluster-admin
   subjects:
   - kind: ServiceAccount
     name: admin-user
     namespace: kubernetes-dashboard
   ```

2. 执行生成

   ```sh
   kubectl apply -f dash.yaml
   ```
   
3. 生成令牌

   ```sh
   kubectl -n kubernetes-dashboard get secret $(kubectl -n kubernetes-dashboard get sa/admin-user -o jsonpath="{.secrets[0].name}") -o go-template="{{.data.token | base64decode}}"
   ```

   ```
   eyJhbGciOiJSUzI1NiIsImtpZCI6ImF4cFNFYVYtaW9pMU1kUFctek1Qb3BucFAyaWpORWg4R3JDZFZPRzlmamsifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLTJxcmN4Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI4OGEwZGY0Zi0yZTkzLTQzNTQtOTFmYi00ODA3ZGExNDlhM2MiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZXJuZXRlcy1kYXNoYm9hcmQ6YWRtaW4tdXNlciJ9.jAKl2pvtynOU941vXwnTGU2YRzhMmkh67IGqQk_K-5TO3goNDuVyCVJI_fwprMjKRYsIrXQdUW6EBFQAkSKt-UWhlg8nwLm5qc3ggd34X7Q-l4EUT-CtwSgSHKgzp5tnGI2909QilFAA5NP71pirxVwOamDjIPJoG3Cla1WQL9vjglIK64dQEfdi60GmET6OT9ePc5PwAl32hP0n9JQ7PBIZdCLuL5qxcQ6dHbUSIJQ9vcnIGW_YBZny2z6q4klqZVgzXrnBmkFvGJTi5yki8oV0WoalLJudnniMs3FoVxXDxQqwEmidst1HEK4BSyYJraMkDdCQkwGVD4oDIMYgIA
   ```

4. 访问后台连接，`https://三个节点随便一个公共IP:32599/`,把上面生成的token复制进去进如后台

# `Kubernetes`核心实战

资源创建方式

- 命令行
- YAML

## `Namespace`

### 命令行方式

> 名称空间用来隔离资源

```sh
# 获取名称空间
[root@k8s-master ~]# kubectl get ns
NAME                   STATUS   AGE
default                Active   10h
kube-node-lease        Active   10h
kube-public            Active   10h
kube-system            Active   10h
kubernetes-dashboard   Active   8h
```

```sh
#获取K8s下所有的应用
[root@k8s-master ~]# kubectl get pods -A
NAMESPACE              NAME                                         READY   STATUS    RESTARTS   AGE
kube-system            calico-kube-controllers-756dd4db79-xjq6h     1/1     Running   1          10h
kube-system            calico-node-8hhzx                            1/1     Running   1          10h
kube-system            calico-node-brz27                            1/1     Running   0          8h
kube-system            calico-node-slhss                            1/1     Running   0          8h
kube-system            coredns-7f89b7bc75-s9t2s                     1/1     Running   1          10h
kube-system            coredns-7f89b7bc75-wl4pb                     1/1     Running   1          10h
kube-system            etcd-k8s-master                              1/1     Running   1          10h
kube-system            kube-apiserver-k8s-master                    1/1     Running   1          10h
kube-system            kube-controller-manager-k8s-master           1/1     Running   1          10h
kube-system            kube-proxy-hf4f8                             1/1     Running   0          8h
kube-system            kube-proxy-kdr8z                             1/1     Running   1          10h
kube-system            kube-proxy-r4w2l                             1/1     Running   0          8h
kube-system            kube-scheduler-k8s-master                    1/1     Running   1          10h
kubernetes-dashboard   dashboard-metrics-scraper-79c5968bdc-9sdpv   1/1     Running   0          8h
kubernetes-dashboard   kubernetes-dashboard-658485d5c7-xq6rq        1/1     Running   0          8h
```

> ```sh
> [root@k8s-master ~]# kubectl get pods
> No resources found in default namespace.
> ```
>
> 只会获取默认命令空间下的`default `下的应用，如果在布署时不指定命名空间，都是默认指定在`default`下

```sh
#查看指定命名空间下的应用
[root@k8s-master ~]# kubectl get pods -n kubernetes-dashboard
NAME                                         READY   STATUS    RESTARTS   AGE
dashboard-metrics-scraper-79c5968bdc-9sdpv   1/1     Running   0          8h
kubernetes-dashboard-658485d5c7-xq6rq        1/1     Running   0          8h
```

```sh
# 默认的名称空间是不允许删除的（创建的则可以）
[root@k8s-master ~]# kubectl delete ns default
Error from server (Forbidden): namespaces "default" is forbidden: this namespace may not be deleted
[root@k8s-master ~]# kubectl create ns hello	#创建自定义名称空间
namespace/hello created
[root@k8s-master ~]# kubectl get ns
NAME                   STATUS   AGE
default                Active   10h
hello                  Active   9s
kube-node-lease        Active   10h
kube-public            Active   10h
kube-system            Active   10h
kubernetes-dashboard   Active   9h
[root@k8s-master ~]# kubectl delete ns hello	#删除自定义名称空间
namespace "hello" deleted
```

### 资源配置方式

```yaml
apiVersion: v1	#固定
kind: Namespace	#固定
metadata:
  name: hello
```

```sh
[root@k8s-master ~]# kubectl apply -f hello.yaml 	#执行上面自定义的资源文件创建名称空间
namespace/hello created
[root@k8s-master ~]# kubectl get ns -A
NAME                   STATUS   AGE
default                Active   11h
hello                  Active   3s
kube-node-lease        Active   11h
kube-public            Active   11h
kube-system            Active   11h
kubernetes-dashboard   Active   9h
[root@k8s-master ~]# kubectl delete -f hello.yaml 	#删除自定义的名称空间（一般资源文件方式创建的使用此方式删除）
namespace "hello" deleted
```

## Pod

> 运行中的一组容器，Pod是kubernetes中应用的最小单位.

```sh
# 创建pod
[root@k8s-master ~]# kubectl run mynginx --image=nginx
pod/mynginx created

# 删除pod
[root@k8s-master ~]# kubectl delete pod mynginx
pod "mynginx" deleted
```

### 命令行方式

```sh
# 查看default名称空间的Pod
[root@k8s-master ~]# kubectl get pod
NAME      READY   STATUS    RESTARTS   AGE
mynginx   1/1     Running   0          32s
# 描述
#kubectl describe pod 你自己的Pod名字
[root@k8s-master ~]# kubectl describe pod mynginx
Name:         mynginx
Namespace:    default
Priority:     0
Node:         k8s-node2/192.168.186.134	#在node2节点下生成了执行应用，可以使用docker 命令在node2查询到，master和node无法查询到
Start Time:   Mon, 06 Dec 2021 08:43:54 +0800
Labels:       run=mynginx
Annotations:  cni.projectcalico.org/containerID: 70907ca260db3fd65e5e432851972b9a0519bf02a5442bc8b169458ed53344cd
              cni.projectcalico.org/podIP: 10.244.169.130/32
              cni.projectcalico.org/podIPs: 10.244.169.130/32
Status:       Running
IP:           10.244.169.130
IPs:
  IP:  10.244.169.130
Containers:
  mynginx:
    Container ID:   docker://af42da2cfb2c66912139f251386ac545788408e03d282e5b4d1b5da6c4f4cf7d
    Image:          nginx
    Image ID:       docker-pullable://nginx@sha256:9522864dd661dcadfd9958f9e0de192a1fdda2c162a35668ab6ac42b465f0603
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Mon, 06 Dec 2021 08:44:25 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-sbrfk (ro)
Conditions:
  Type              Status
  Initialized       True 
  Ready             True 
  ContainersReady   True 
  PodScheduled      True 
Volumes:
  default-token-sbrfk:
    Type:        Secret (a volume populated by a Secret)
    SecretName:  default-token-sbrfk
    Optional:    false
QoS Class:       BestEffort
Node-Selectors:  <none>
Tolerations:     node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                 node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
#应用事件，可以看到应用在k8s-node2成功拉取创建并启动
Events:
  Type    Reason     Age   From               Message
  ----    ------     ----  ----               -------
  Normal  Scheduled  94s   default-scheduler  Successfully assigned default/mynginx to k8s-node2
  Normal  Pulling    92s   kubelet            Pulling image "nginx"
  Normal  Pulled     64s   kubelet            Successfully pulled image "nginx" in 28.739614085s
  Normal  Created    63s   kubelet            Created container mynginx
  Normal  Started    63s   kubelet            Started container mynginx
```

```sh
# 查看Pod的运行日志
[root@k8s-master ~]# kubectl logs mynginx
/docker-entrypoint.sh: /docker-entrypoint.d/ is not empty, will attempt to perform configuration
/docker-entrypoint.sh: Looking for shell scripts in /docker-entrypoint.d/
/docker-entrypoint.sh: Launching /docker-entrypoint.d/10-listen-on-ipv6-by-default.sh
10-listen-on-ipv6-by-default.sh: info: Getting the checksum of /etc/nginx/conf.d/default.conf
10-listen-on-ipv6-by-default.sh: info: Enabled listen on IPv6 in /etc/nginx/conf.d/default.conf
/docker-entrypoint.sh: Launching /docker-entrypoint.d/20-envsubst-on-templates.sh
/docker-entrypoint.sh: Launching /docker-entrypoint.d/30-tune-worker-processes.sh
/docker-entrypoint.sh: Configuration complete; ready for start up
2021/12/06 00:44:25 [notice] 1#1: using the "epoll" event method
2021/12/06 00:44:25 [notice] 1#1: nginx/1.21.4
2021/12/06 00:44:25 [notice] 1#1: built by gcc 10.2.1 20210110 (Debian 10.2.1-6) 
2021/12/06 00:44:25 [notice] 1#1: OS: Linux 3.10.0-1160.49.1.el7.x86_64
2021/12/06 00:44:25 [notice] 1#1: getrlimit(RLIMIT_NOFILE): 1048576:1048576
2021/12/06 00:44:25 [notice] 1#1: start worker processes
2021/12/06 00:44:25 [notice] 1#1: start worker process 30
2021/12/06 00:44:25 [notice] 1#1: start worker process 31
```

### 资源配置方式

```yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: mynginx
  name: mynginx
#  namespace: default	#指定命名空间，不指定就是default
spec:
  containers:
  - image: nginx
    name: mynginx
```

```sh
#资源方式创建pod
[root@k8s-master ~]# kubectl apply -f mynginx.yaml 
pod/mynginx created
#资源方式删除pod
[root@k8s-master ~]# kubectl delete -f mynginx.yaml 
pod "mynginx" deleted
```

```sh
# 每个Pod - k8s都会分配一个ip
[root@master ~]# kubectl get pod -owide
NAME      READY   STATUS    RESTARTS   AGE   IP                NODE    NOMINATED NODE   READINESS GATES
mynginx   1/1     Running   0          16s   192.168.166.131   node1   <none>           <none>

[root@master ~]# curl 192.168.166.131
# 集群中的任意一个机器以及任意的应用都能通过Pod分配的ip来访问这个Pod

# 进入pod应用
[root@k8s-master ~]# kubectl exec -it mynginx -- /bin/bash
root@mynginx:/# 
```

### 多个资源配置

```yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: myapp
  name: myapp
spec:
  containers:
  - image: nginx
    name: nginx
  - image: tomcat:8.5.68
    name: tomcat
```

```sh
[root@k8s-master ~]# kubectl apply -f multi.yaml 
pod/myapp created
# 当myapp 2个都成功执行才算成功
[root@k8s-master ~]# kubectl get pod
NAME      READY   STATUS    RESTARTS   AGE
myapp     2/2     Running   0          53s
mynginx   1/1     Running   0          29m
```

```sh
# 进入应用，两个容器间的访问是互通的，通过127.0.0.1
[root@k8s-master ~]# kubectl exec -it myapp -- /bin/bash
Defaulting container name to nginx.
Use 'kubectl describe pod/myapp -n default' to see all of the containers in this pod.
root@myapp:/# curl 127.0.0.1:8080 
<!doctype html><html lang="en"><head><title>HTTP Status 404 – Not Found</title><style type="text/css">body {font-family:Tahoma,Arial,sans-serif;} h1, h2, h3, b {color:white;background-color:#525D76;} h1 {font-size:22px;} h2 {font-size:16px;} h3 {font-size:14px;} p {font-size:12px;} a {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style></head><body><h1>HTTP Status 404 – Not Found</h1><hr class="line" /><p><b>Type</b> Status Report</p><p><b>Description</b> The origin server did not find a current representation for the target resource or is not willing to disclose that one exists.</p><hr class="line" /><h3>Apache Tomcat/8.5.68</h3></body></html>root@myapp:/# curl 127.0.0.1:80
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
```

> 注意:当应用里有同一种的两个容器，会占用同端口异常无法都启动成功，例如同一个`pod`里有两个`Nginx`

## Deployment

> 控制Pod，使Pod拥有多副本，自愈，扩缩容等能力

```sh
# 创建依赖，无论如何删除，都会重新创建一个pod
[root@k8s-master ~]# kubectl create deployment mytomcat --image=tomcat:8.5.68
deployment.apps/mytomcat created
[root@k8s-master ~]# kubectl get pod
NAME                        READY   STATUS    RESTARTS   AGE
mytomcat-6f5f895f4f-hrn9q   1/1     Running   0          29

[root@k8s-master ~]# kubectl delete pod mytomcat-6f5f895f4f-hrn9q
pod "mytomcat-6f5f895f4f-hrn9q" deleted
#可以看到如下又重新创建了新的应用，这就是自愈能力，就算节点宕机重启都可以重新拉起
[root@k8s-master ~]# kubectl get pod
NAME                        READY   STATUS    RESTARTS   AGE
mytomcat-6f5f895f4f-spd75   1/1     Running   0          59s
```

### 多副本

```sh
# 查看deploy
[root@k8s-master ~]# kubectl get deploy
NAME       READY   UP-TO-DATE   AVAILABLE   AGE
mytomcat   1/1     1            1           3h5m

# 删除deploy
[root@k8s-master ~]# kubectl delete deploy mytomcat
deployment.apps "mytomcat" deleted

# 创建多台机器的副本
[root@k8s-master ~]# kubectl create deployment my-dep --image=nginx --replicas=3
deployment.apps/my-dep created
[root@k8s-master ~]# kubectl get deploy
NAME     READY   UP-TO-DATE   AVAILABLE   AGE
my-dep   3/3     3            3           38s
```

资源配置方式

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: my-dep
  name: my-dep
spec:
  replicas: 5
  selector:
    matchLabels:
      app: my-dep
  template:
    metadata:
      labels:
        app: my-dep
    spec:
      containers:
      - image: nginx
        name: nginx
```

```sh
[root@k8s-master ~]# kubectl get deploy
NAME     READY   UP-TO-DATE   AVAILABLE   AGE
my-dep   5/5     5            5           58s
```

### 扩缩容

```sh
#扩容，之前3个应用，现在可以扩容到5个
[root@k8s-master ~]# kubectl scale --replicas=5 deploy/my-dep
[root@k8s-master ~]# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5b7868d854-4xhdf   1/1     Running   0          4m55s
my-dep-5b7868d854-5jh8p   1/1     Running   0          4m55s
my-dep-5b7868d854-dg5vf   1/1     Running   0          4m55s
my-dep-5b7868d854-pz5wd   1/1     Running   0          56s
my-dep-5b7868d854-r649w   1/1     Running   0          56s

# 缩容，把上面的5个应用缩到2个
[root@k8s-master ~]# kubectl scale --replicas=2 deploy/my-dep
deployment.apps/my-dep scaled
[root@k8s-master ~]# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5b7868d854-4xhdf   1/1     Running   0          6m43s
my-dep-5b7868d854-5jh8p   1/1     Running   0          6m43s
```

```sh
# 通过修改配置进行扩容
[root@k8s-master ~]# kubectl edit deploy my-dep
deployment.apps/my-dep edited
```

生成如下配置文件

```yaml
# Please edit the object below. Lines beginning with a '#' will be ignored,
# and an empty file will abort the edit. If an error occurs while saving this file will be
# reopened with the relevant failures.
#
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    deployment.kubernetes.io/revision: "1"
  creationTimestamp: "2021-12-06T06:27:58Z"
  generation: 3
  labels:
    app: my-dep
  name: my-dep
  namespace: default
  resourceVersion: "47283"
  uid: d6e8bebd-8077-4066-9b2d-20e9f19eea86
spec:
  progressDeadlineSeconds: 600
  replicas: 2	#这里修改成5，扩容
  revisionHistoryLimit: 10
  selector:
    matchLabels:
      app: my-dep
  strategy:
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
    
...
```

### 自愈&故障转移

当有一台机器宕机了，K8s会保证应用的个数，在另一台机器启动多另一个应用

```sh
# 追踪应用事件
[root@k8s-master ~]# kubectl get pod -w
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5b7868d854-5jh8p   1/1     Running   0          28m
my-dep-5b7868d854-sdl2b   1/1     Running   0          18m
my-dep-5b7868d854-sv7b2   1/1     Running   0          18m
my-dep-5b7868d854-5jh8p   1/1     Terminating   0          28m
my-dep-5b7868d854-vjgwk   0/1     Pending       0          0s
my-dep-5b7868d854-vjgwk   0/1     Pending       0          0s
my-dep-5b7868d854-vjgwk   0/1     ContainerCreating   0          0s
my-dep-5b7868d854-vjgwk   0/1     ContainerCreating   0          1s
my-dep-5b7868d854-vjgwk   1/1     Running             0          17s
```

> 上面当`Node1`宕机后，会在`Node2`启动新的一个应用

### 滚动更新

不停机直接更新指定的应用，当一个应用启动成功，会把当前容器里的同一个应用再删除

```sh
[root@k8s-master ~]# kubectl set image deployment/my-dep nginx=nginx:1.16.1 --record
deployment.apps/my-dep image updated
```

### 版本回退

```sh
#历史记录
deployment.apps/my-dep 
REVISION  CHANGE-CAUSE
1         <none>
2         kubectl set image deployment/my-dep nginx=nginx:1.16.1 --record=true

#查看某个历史详情
[root@k8s-master ~]# kubectl rollout history deployment/my-dep --revision=2
deployment.apps/my-dep with revision #2
Pod Template:
  Labels:	app=my-dep
	pod-template-hash=6b48cbf4f9
  Annotations:	kubernetes.io/change-cause: kubectl set image deployment/my-dep nginx=nginx:1.16.1 --record=true
  Containers:
   nginx:
    Image:	nginx:1.16.1
    Port:	<none>
    Host Port:	<none>
    Environment:	<none>
    Mounts:	<none>
  Volumes:	<none>

#回滚(回到上次)
[root@k8s-master ~]# kubectl rollout undo deployment/my-dep
deployment.apps/my-dep rolled back

#回滚(回到指定版本)
kubectl rollout undo deployment/my-dep --to-revision=2
```

> 更多：
>
> 除了Deployment，k8s还有 `StatefulSet` 、`DaemonSet` 、`Job`  等 类型资源。我们都称为 `工作负载`。
>
> 有状态应用使用  `StatefulSet`  部署，无状态应用使用 `Deployment` 部署
>
> https://kubernetes.io/zh/docs/concepts/workloads/controllers/

## Service

> 将一组 [Pods](https://kubernetes.io/docs/concepts/workloads/pods/pod-overview/) 公开为网络服务的抽象方法。

### ClusterIP

```shell
#获取上面依赖布署的三个pod
[root@master ~]# kubectl get pod -owide
NAME                      READY   STATUS    RESTARTS   AGE     IP                NODE    NOMINATED NODE   READINESS GATES
my-dep-5b7868d854-jw5qx   1/1     Running   0          6m28s   192.168.166.133   node1   <none>           <none>
my-dep-5b7868d854-w46jh   1/1     Running   0          6m28s   192.168.104.3     node2   <none>           <none>
my-dep-5b7868d854-w8qht   1/1     Running   0          6m28s   192.168.166.132   node1   <none>           <none>

[root@master ~]# curl 192.168.166.132
333
[root@master ~]# curl 192.168.166.133
111
[root@master ~]# curl 192.168.104.3
222

#暴露Deploy，集群内使用service的ip:port就可以负载均衡的访问
# 等同于没有--type的
#kubectl expose deployment my-dep --port=8000 --target-port=80 --type=ClusterIP	集群内部的访问
[root@k8s-master ~]# kubectl expose deployment my-dep --port=8000 --target-port=80
service/my-dep exposed

[root@master ~]# kubectl get service
NAME         TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1     <none>        443/TCP    11h
my-dep       ClusterIP   10.96.8.118   <none>        8000/TCP   3s

[root@master ~]# curl 10.96.8.118:8000
222
[root@master ~]# curl 10.96.8.118:8000
111
[root@master ~]# curl 10.96.8.118:8000
333

#注意：下面域名方式的访问只能在容器里进行访问，进入任意一个容器
#服务名.所在名称空间.svc
#my-dep.default.svc:8000
root@my-dep-5b7868d854-w8qht:/# curl my-dep.default.svc:8000
333

```

资源配置方式

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: my-dep
  name: my-dep
spec:
  selector:
    app: my-dep
  ports:
  - port: 8000
    protocol: TCP
    targetPort: 80
```

```sh
#显示标签
[root@k8s-master ~]# kubectl get pod --show-labels
NAME                      READY   STATUS    RESTARTS   AGE   LABELS
my-dep-5b7868d854-9lb8d   1/1     Running   0          40m   app=my-dep,pod-template-hash=5b7868d854
my-dep-5b7868d854-lnqd9   1/1     Running   0          39m   app=my-dep,pod-template-hash=5b7868d854
my-dep-5b7868d854-vzqx2   1/1     Running   0          39m   app=my-dep,pod-template-hash=5b7868d854

#使用标签检索Pod，根据上面的LABLES
kubectl get pod -l app=my-dep
```

### NodePort

```sh
#kubectl expose deployment my-dep --port=8000 --target-port=80 --type=NodePort #集群外部也可以访问
[root@master ~]# kubectl expose deployment my-dep --port=8000 --target-port=80 --type=NodePort
service/my-dep exposed
[root@master ~]# kubectl get service
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP          13h
my-dep       NodePort    10.96.143.147   <none>        8000:30954/TCP   3s
```

> NodePort范围在 `30000-32767` 之间（注意：**开放云服务安全访问端口**）
>
> 这时，随便访问`公网IP:30954`就可访问到对外部署的应用

## Ingress

### 安装

```sh
wget https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v0.47.0/deploy/static/provider/baremetal/deploy.yaml

#修改镜像
vim deploy.yaml

#源镜像是国外 docker.io，拉取会失败，调整为下面私人仓库
#将image的值改为如下值：
registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/ingress-nginx-controller:v0.46.0

# 检查安装的结果
kubectl get pod,svc -n ingress-nginx

# 最后别忘记把svc暴露的端口要放行
[root@master ~]# kubectl get svc -A
NAMESPACE              NAME                                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
default                kubernetes                           ClusterIP   10.96.0.1       <none>        443/TCP                      15h
default                my-dep                               NodePort    10.96.143.147   <none>        8000:30954/TCP               96m
ingress-nginx          ingress-nginx-controller             NodePort    10.96.199.154   <none>        80:30025/TCP,443:32134/TCP   29m
ingress-nginx          ingress-nginx-controller-admission   ClusterIP   10.96.140.235   <none>        443/TCP                      29m
kube-system            kube-dns                             ClusterIP   10.96.0.10      <none>        53/UDP,53/TCP,9153/TCP       15h
kubernetes-dashboard   dashboard-metrics-scraper            ClusterIP   10.96.177.150   <none>        8000/TCP                     15h
kubernetes-dashboard   kubernetes-dashboard  
```

### 使用

官网地址：https://kubernetes.github.io/ingress-nginx/

就是nginx做的

- https://公网IP:32401/
- http://公网IP:31405/

测试环境

> 应用如下yaml，准备好测试环境

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hello-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: hello-server
  template:
    metadata:
      labels:
        app: hello-server
    spec:
      containers:
      - name: hello-server
        image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/hello-server
        ports:
        - containerPort: 9000
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx-demo
  name: nginx-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx-demo
  template:
    metadata:
      labels:
        app: nginx-demo
    spec:
      containers:
      - image: nginx
        name: nginx
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: nginx-demo
  name: nginx-demo
spec:
  selector:
    app: nginx-demo
  ports:
  - port: 8000
    protocol: TCP
    targetPort: 80
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: hello-server
  name: hello-server
spec:
  selector:
    app: hello-server
  ports:
  - port: 8000
    protocol: TCP
    targetPort: 9000
```

#### 域名访问

   ```yaml
   apiVersion: networking.k8s.io/v1
   kind: Ingress  
   metadata:
     name: ingress-host-bar
   spec:
     ingressClassName: nginx
     rules:
     - host: "hello.test.com"
       http:
         paths:
         - pathType: Prefix
           path: "/"
           backend:
             service:
               name: hello-server
               port:
                 number: 8000
     - host: "demo.test.com"
       http:
         paths:
         - pathType: Prefix
           path: "/"  # 当配置了/nginx有路径时，把请求会转给下面的服务，下面的服务一定要能处理这个路径，不能处理就是404
           backend:
             service:
               name: nginx-demo  ## java，比如使用路径重写，去掉前缀nginx
               port:
                 number: 8000
   ```

   ```sh
   # 布署上面的测试文件 
   [root@master ~]# kubectl apply -f test.yaml 
   ingress.networking.k8s.io/ingress-host-bar created
   [root@master ~]# kubectl get svc -A
   NAMESPACE              NAME                                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                      AGE
   default                hello-server                         ClusterIP   10.96.62.215    <none>        8000/TCP                     44m
   default                kubernetes                           ClusterIP   10.96.0.1       <none>        443/TCP                      19h
   default                my-dep                               NodePort    10.96.143.147   <none>        8000:30954/TCP               5h28m
   default                nginx-demo                           ClusterIP   10.96.116.154   <none>        8000/TCP                     44m
   ingress-nginx          ingress-nginx-controller             NodePort    10.96.199.154   <none>        80:30025/TCP,443:32134/TCP   4h21m
   ingress-nginx          ingress-nginx-controller-admission   ClusterIP   10.96.140.235   <none>        443/TCP                      4h21m
   kube-system            kube-dns                             ClusterIP   10.96.0.10      <none>        53/UDP,53/TCP,9153/TCP       19h
   kubernetes-dashboard   dashboard-metrics-scraper            ClusterIP   10.96.177.150   <none>        8000/TCP                     18h
   kubernetes-dashboard   kubernetes-dashboard                 NodePort    10.96.74.65     <none>        443:32599/TCP                18h
   
   ```

   修改本地`hosts`文件，分别配置

   ```
   8.134.37.41 hello.test.com
   8.134.37.41 demo.test.com
   ```

   根据上面的配置访问http://hello.test.com:30025/和 http://demo.test.com:30025/,可以看到可以根据域名不同访问不同的应用

   > 问题： `path: "/nginx"` 与  `path: "/"` 为什么会有不同的效果？
   >
   > ```sh
   > # 获取ing 
   > [root@master ~]# kubectl get ing
   > NAME               CLASS   HOSTS                          ADDRESS        PORTS   AGE
   > ingress-host-bar   nginx   hello.test.com,demo.test.com   172.16.0.116   80      27m
   > #修改 ing 配置
   > [root@master ~]# kubectl edit ing
   > ingress.networking.k8s.io/ingress-host-bar edited
   > ```
   >
   > 把上面配置文件中访问`demo.test.com`下的 `path: "/" `改成 `path: "/nginx" `
   >
   > 在`nginx-demo`下修改添加
   >
   > ```sh
   > cd "usr/share/nginx/html"
   > mkdir nginx
   > echo 1111 > index.html
   > [root@master ~]# curl http://demo.test.com:30025/nginx/
   > 1111
   > ```
   >
   > 可以看到当访问带有path路径的情况下，就是匹配容器下的访问路径

路径重写

   官网地址：https://kubernetes.github.io/ingress-nginx/examples/rewrite/#rewrite-target

   ```yaml
   apiVersion: networking.k8s.io/v1
   kind: Ingress  
   metadata:
     annotations:
       nginx.ingress.kubernetes.io/rewrite-target: /$2		#这里加重写
     name: ingress-host-bar
   spec:
     ingressClassName: nginx
     rules:
     - host: "hello.test.com"
       http:
         paths:
         - pathType: Prefix
           path: "/"
           backend:
             service:
               name: hello-server
               port:
                 number: 8000
     - host: "demo.test.com"
       http:
         paths:
         - pathType: Prefix
           path: "/nginx(/|$)(.*)"  # 把请求会转给下面的服务，下面的服务一定要能处理这个路径，不能处理就是404
           backend:
             service:
               name: nginx-demo  ## java，比如使用路径重写，去掉前缀nginx
               port:
                 number: 8000
   ```

   > 在这个入口定义中，被捕获的任何字符`(.*)`都将分配给占位符`$2`，然后将其用作`rewrite-target`注释中的参数。
   >
   > 例如，上面的入口定义将导致以下重写：
   >
   > - `rewrite.bar.com/something` 改写为 `rewrite.bar.com/`
   > - `rewrite.bar.com/something/` 改写为 `rewrite.bar.com/`
   > - `rewrite.bar.com/something/new` 改写为 `rewrite.bar.com/new`

   ```sh
   # 现在访问带有/path路径的都会自动转到/目录下的页面
   $ curl http://demo.test.com:30025/nginx/
   hello nginx
   ```

#### 流量限制

   官网链接:https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/configmap/#limit-rate

   ```yaml
   apiVersion: networking.k8s.io/v1
   kind: Ingress
   metadata:
     name: ingress-limit-rate
     annotations:
       nginx.ingress.kubernetes.io/limit-rps: "1"
   spec:
     ingressClassName: nginx
     rules:
     - host: "haha.test.com"
       http:
         paths:
         - pathType: Exact
           path: "/"
           backend:
             service:
               name: nginx-demo
               port:
                 number: 8000
   ```

   ```sh
   #当访问速度过快时会看到打印如下信息
   $ curl http://haha.test.com:30025
   <html>
   <head><title>503 Service Temporarily Unavailable</title></head>
   <body>
   <center><h1>503 Service Temporarily Unavailable</h1></center>
   <hr><center>nginx</center>
   </body>
   </html>
   ```

## 存储抽象

### 环境准备

1. 所有节点

   ```sh
   #所有机器安装
   yum install -y nfs-utils
   ```

2. 主节点

   ```sh
   #nfs主节点
   echo "/nfs/data/ *(insecure,rw,sync,no_root_squash)" > /etc/exports
   
   mkdir -p /nfs/data
   systemctl enable rpcbind --now
   systemctl enable nfs-server --now
   #配置生效
   exportfs -r
   ```

3. 从节点

   ```sh
   showmount -e 172.16.0.117  #注意 ，这里的IP是主节点的内网IP
   
   #执行以下命令挂载 nfs 服务器上的共享目录到本机路径 /root/nfsmount
   mkdir -p /nfs/data
   
   mount -t nfs 172.16.0.117:/nfs/data /nfs/data
   # 写入一个测试文件
   echo "hello nfs server" > /nfs/data/test.txt
   
   #只要其一节点修改文件,就可以在其它主从节点看到上面文件变化
   ```
   
4. 原生方式数据挂载

   ```yaml
   #vim nfs.yaml
   
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     labels:
       app: nginx-pv-demo
     name: nginx-pv-demo
   spec:
     replicas: 2
     selector:
       matchLabels:
         app: nginx-pv-demo
     template:
       metadata:
         labels:
           app: nginx-pv-demo
       spec:
         containers:
         - image: nginx
           name: nginx
           volumeMounts:
           - name: html
             mountPath: /usr/share/nginx/html	#容器内需要挂载到外面的目录
         volumes:
           - name: html	#对应上面的要挂载的name
             nfs:
               server: 172.16.0.117	#挂载的主节点IP
               path: /nfs/data/nginx-pv #挂载的主节点目录路径，需要事先创建
   ```

   ```sh
   [root@master ~]# kubectl apply -f nfs.yaml 
   deployment.apps/nginx-pv-demo created
   [root@master ~]# kubectl deploy 
   Error: unknown command "deploy" for "kubectl"
   Run 'kubectl --help' for usage.
   [root@master ~]# kubectl get deploy 
   NAME            READY   UP-TO-DATE   AVAILABLE   AGE
   hello-server    2/2     2            2           3h26m
   my-dep          3/3     3            3           10h
   nginx-demo      2/2     2            2           3h26m
   nginx-pv-demo   2/2     2            2           33s
   
   #修改Node节点容器内的nginx，可以看到其它节点都成功同步到数据
   [root@master ~]# cd /nfs/data/nginx-pv/
   [root@master nginx-pv]# ll
   total 4
   -rw-r--r-- 1 root root 5 Dec  7 17:57 index.html
   [root@master nginx-pv]# cat index.html 	
   3333
   ```

### PV&PVC

> PV：持久卷（Persistent Volume），将应用需要持久化的数据保存到指定位置
>
> PVC：持久卷申明（**Persistent Volume Claim**），申明需要使用的持久卷规格

#### 创建pv池

> 静态供应

```sh
#nfs主节点
mkdir -p /nfs/data/01
mkdir -p /nfs/data/02
mkdir -p /nfs/data/03
```

> 创建PV

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv01-10m		#名字随意
spec:
  capacity:
    storage: 10M		#开辟的空间容量
  accessModes:
    - ReadWriteMany		#可读可写多节点
  storageClassName: nfs	#容量名 可随意 ，但要对应下面的
  nfs:
    path: /nfs/data/01
    server: 172.16.0.117	#主节点私有IP
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv02-1gi
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteMany
  storageClassName: nfs
  nfs:
    path: /nfs/data/02
    server: 172.16.0.117
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv03-3gi
spec:
  capacity:
    storage: 3Gi
  accessModes:
    - ReadWriteMany
  storageClassName: nfs
  nfs:
    path: /nfs/data/03
    server: 172.16.0.117
```

```sh
[root@master ~]# vim pv.yaml #创建上面文件yaml文件
[root@master ~]# kubectl apply -f pv.yaml 
persistentvolume/pv01-10m created
persistentvolume/pv02-1gi created
persistentvolume/pv03-3gi created
#可以看到已经成功创建了三个空间
[root@master ~]# kubectl get pv
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available           nfs                     3m15s
pv02-1gi   1Gi        RWX            Retain           Available           nfs                     3m15s
pv03-3gi   3Gi        RWX            Retain           Available           nfs                     3m15s
```

#### PVC创建与绑定

> 创建PVC

```yaml
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: nginx-pvc
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 200Mi
  storageClassName: nfs	#对应上面的torageClassName: nfs
```

```sh
[root@master ~]# vim pvc.yaml
[root@master ~]# kubectl apply -f pvc.yaml 
persistentvolumeclaim/nginx-pvc created
[root@master ~]# kubectl get pvc
NAME        STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
nginx-pvc   Bound    pv02-1gi   1Gi        RWX            nfs            4s
# 注意 可以看到在pv02-1gi下开辟了一个200m的空间，为什么是在pvpv02-1gi呢
# pv01-10m明显只有10m,不够用, pv03-3gi 空间太大没必要
[root@master ~]# kubectl get pv
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     5m19s
pv02-1gi   1Gi        RWX            Retain           Bound       default/nginx-pvc   nfs                     5m19s
pv03-3gi   3Gi        RWX            Retain           Available                       nfs                     5m19s

#删除PVC挂载，留意下面的状态在释放ing
[root@master ~]# kubectl delete -f pvc.yaml 
persistentvolumeclaim "nginx-pvc" deleted
[root@master ~]# kubectl get pv
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     21m
pv02-1gi   1Gi        RWX            Retain           Released    default/nginx-pvc   nfs                     21m
pv03-3gi   3Gi        RWX            Retain           Available                       nfs                     21m

# 释放中是不能使用的，只能重新在pv03-3gi下分配
[root@master ~]# kubectl apply -f pvc.yaml 
persistentvolumeclaim/nginx-pvc created
[root@master ~]# kubectl get pv
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     22m
pv02-1gi   1Gi        RWX            Retain           Released    default/nginx-pvc   nfs                     22m
pv03-3gi   3Gi        RWX            Retain           Bound       default/nginx-pvc   nfs                     22m
```

> 注意：上面PVC一般不是单独使用的，要指定对应哪个目录进行使用，如下

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx-deploy-pvc
  name: nginx-deploy-pvc
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx-deploy-pvc
  template:
    metadata:
      labels:
        app: nginx-deploy-pvc
    spec:
      containers:
      - image: nginx
        name: nginx
        volumeMounts:
        - name: html
          mountPath: /usr/share/nginx/html
      volumes:
        - name: html
          persistentVolumeClaim:		#这与上面传统的挂载方式区别在这，前面传统的是使用nfs，这里使用的pvc方式进行挂载
            claimName: nginx-pvc
```

现在进入上面创建的`nginx-deploy-pvc`应用,随意在`/usr/share/nginx/html`下创建文件夹，可以在对应挂载的`pv03-3gi`下找到挂载的文件

### ConfigMap

> 抽取应用配置，并且可以自动更新

redis示例

1. 把之前的配置文件创建为配置集

   ```sh
   # 创建配置，redis保存到k8s的etcd；
   [root@master ~]# touch redis.conf 
   [root@master ~]# kubectl create cm redis-conf --from-file=redis.conf 
   configmap/redis-conf created
   #上面保存成功就可以把上面的文件删除了
   [root@master ~]# rm -rf redis.conf 
   [root@master ~]# kubectl get cm
   NAME               DATA   AGE
   kube-root-ca.crt   1      24h
   redis-conf         1      57s
   
   ```

   ```yaml
   apiVersion: v1
   data:    #data是所有真正的数据，key：默认是文件名   value：配置文件的内容
     redis-123.conf: |
       appendonly yes
   kind: ConfigMap
   metadata:
     name: redis-conf
     namespace: default
   ```

   ```sh
   #生成上面的配置集
   [root@master ~]# kubectl apply -f redis-123.yaml 
   Warning: resource configmaps/redis-conf is missing the kubectl.kubernetes.io/last-applied-configuration annotation which is required by kubectl apply. kubectl apply should only be used on resources created declaratively by either kubectl create --save-config or kubectl apply. The missing annotation will be patched automatically.
   configmap/redis-conf configured
   ```

2. 创建`Pod`

   ```yaml
   apiVersion: v1
   kind: Pod
   metadata:
     name: redis
   spec:
     containers:
     - name: redis
       image: redis
       command:
         - redis-server
         - "/redis-master/redis.conf"  #指的是redis容器内部的位置
       ports:
       - containerPort: 6379
       volumeMounts:
       - mountPath: /data	#容器里挂载的数据目录
         name: data	
       - mountPath: /redis-master	#容器里挂载的配置文件路径
         name: config
     volumes:
       - name: data
         emptyDir: {}
       - name: config
         configMap:
           name: redis-conf	#对应上面的配置集的redis-conf
           items:
           - key: redis-123.conf	#对应上面的配置集的 redis-123.conf
             path: redis.conf		#redis加载的配置文件
   ```
   
3. 检查默认配置
   
   ```sh
   kubectl exec -it redis -- redis-cli
   
   127.0.0.1:6379> CONFIG GET appendonly
   ```
   
4. 修改`ConfigMap`

   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: redis-conf
   data:
     redis-123: |
       maxmemory 2mb
       maxmemory-policy allkeys-lru 
   ```

5. 检查配置是否更新

   ```sh
   kubectl exec -it redis -- redis-cli
   
   127.0.0.1:6379> CONFIG GET maxmemory
   127.0.0.1:6379> CONFIG GET maxmemory-policy
   ```
   
   > 检查指定文件内容是否已经更新
   >
   > 修改了CM。Pod里面的配置文件会跟着变
   >
   > 
   >
   > ***配置值未更改，因为需要重新启动 Pod 才能从关联的 ConfigMap 中获取更新的值。\*** 
   >
   > ***原因：我们的Pod部署的中间件自己本身没有热更新能力\***
   

## Secret

> Secret 对象类型用来保存敏感信息，例如密码、OAuth 令牌和 SSH 密钥。 将这些信息放在 secret 中比放在 Pod的定义或者 容器镜像中来说更加安全和灵活。

```sh
kubectl create secret docker-registry leifengyang-docker \
--docker-username=leifengyang \
--docker-password=Lfy123456 \
--docker-email=534096094@qq.com

##命令格式
kubectl create secret docker-registry regcred \
  --docker-server=<你的镜像仓库服务器> \
  --docker-username=<你的用户名> \
  --docker-password=<你的密码> \
  --docker-email=<你的邮箱地址>
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-nginx
spec:
  containers:
  - name: private-nginx
    image: leifengyang/guignginx:v1.0
  imagePullSecrets:
  - name: leifengyang-docker
```



------

# Kubernetes平台安装

官网地址:https://kubesphere.com.cn/

## Kubernetes上安装KubeSphere

### 安装步骤

- 选择4核8G（master）、8核16G（node1）、8核16G（node2） 三台机器，按量付费进行实验，CentOS7.9

- 安装Docker
- 安装Kubernetes

- 安装KubeSphere前置环境
- 安装KubeSphere

#### 安装Docker

```sh
sudo yum remove docker*
sudo yum install -y yum-utils

#配置docker的yum地址
sudo yum-config-manager \
--add-repo \
http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo


#安装指定版本
sudo yum install -y docker-ce-20.10.7 docker-ce-cli-20.10.7 containerd.io-1.4.6

#	启动&开机启动docker
systemctl enable docker --now

# docker加速配置
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://1eac3n45.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

#### 安装Kubernetes

1. 基本环境

   > 每个机器使用内网ip互通
   >
   > 每个机器配置自己的`hostname`，不能用`localhost`

   ```sh
   #设置每个机器自己的hostname
   hostnamectl set-hostname xxx
   
   # 将 SELinux 设置为 permissive 模式（相当于将其禁用）
   sudo setenforce 0
   sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
   
   #关闭swap
   swapoff -a  
   sed -ri 's/.*swap.*/#&/' /etc/fstab
   
   #允许 iptables 检查桥接流量
   cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
   br_netfilter
   EOF
   
   cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
   net.bridge.bridge-nf-call-ip6tables = 1
   net.bridge.bridge-nf-call-iptables = 1
   EOF
   sudo sysctl --system
   ```

2. 安装kubelet、kubeadm、kubectl

   ```java
   #配置k8s的yum源地址
   cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
   [kubernetes]
   name=Kubernetes
   baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
   enabled=1
   gpgcheck=0
   repo_gpgcheck=0
   gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
      http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
   EOF
   
   
   #安装 kubelet，kubeadm，kubectl
   sudo yum install -y kubelet-1.20.9 kubeadm-1.20.9 kubectl-1.20.9
   
   #启动kubelet
   sudo systemctl enable --now kubelet
   
   #所有机器配置master域名
   echo "172.16.0.115  k8s-master" >> /etc/hosts
   ```

3. 初始化master节点

   1. 初始化

      ```sh
      kubeadm init \
      --apiserver-advertise-address=172.16.0.115 \	#主节点内网ip
      --control-plane-endpoint=k8s-master \	#对应上面的hosts配置主节点
      --image-repository registry.aliyuncs.com/google_containers \
      --kubernetes-version v1.20.9 \
      --service-cidr=10.96.0.0/16 \
      --pod-network-cidr=192.168.0.0/16
      ```

   2. 记录关键信息

      > 记录`master`执行完成后的日志

      ```
      Your Kubernetes control-plane has initialized successfully!
      
      To start using your cluster, you need to run the following as a regular user:
      
        mkdir -p $HOME/.kube
        sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
        sudo chown $(id -u):$(id -g) $HOME/.kube/config
      
      Alternatively, if you are the root user, you can run:
      
        export KUBECONFIG=/etc/kubernetes/admin.conf
      
      You should now deploy a pod network to the cluster.
      Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
        https://kubernetes.io/docs/concepts/cluster-administration/addons/
      
      You can now join any number of control-plane nodes by copying certificate authorities
      and service account keys on each node and then running the following as root:
      
        kubeadm join k8s-master:6443 --token dc23kv.utzao1j2n9ehfrnl \
          --discovery-token-ca-cert-hash sha256:7b31b08d9d93af4d2b22c5aa547736ee13ee690254ef81d060adfaf32ead89cb \
          --control-plane 
      
      Then you can join any number of worker nodes by running the following on each as root:
      
      kubeadm join k8s-master:6443 --token dc23kv.utzao1j2n9ehfrnl \
          --discovery-token-ca-cert-hash sha256:7b31b08d9d93af4d2b22c5aa547736ee13ee690254ef81d060adfaf32ead89cb 
      ```
   
      ```sh
      #看到上面完成信息后，在主节点下执行
      mkdir -p $HOME/.kube
      sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
      sudo chown $(id -u):$(id -g) $HOME/.kube/config
      ```
   
   3. 安装Calico网络插件
   
      ```sh
      curl https://docs.projectcalico.org/manifests/calico.yaml -O
      
      kubectl apply -f calico.yaml
      ```
   
   4. 子节点下加入`worker`节点
   
      ```sh
      kubeadm join k8s-master:6443 --token dc23kv.utzao1j2n9ehfrnl \
          --discovery-token-ca-cert-hash sha256:7b31b08d9d93af4d2b22c5aa547736ee13ee690254ef81d060adfaf32ead89cb 
      ```

#### 安装KubeSphere前置环境

1. nfs文件系统

   1. 安装nfs-server

      ```sh
      # 在每个机器。
      yum install -y nfs-utils
      
      
      # 在master 执行以下命令 
      echo "/nfs/data/ *(insecure,rw,sync,no_root_squash)" > /etc/exports
      
      
      # 执行以下命令，启动 nfs 服务;创建共享目录
      mkdir -p /nfs/data
      
      
      # 在master执行
      systemctl enable rpcbind
      systemctl enable nfs-server
      systemctl start rpcbind
      systemctl start nfs-server
      
      # 使配置生效
      exportfs -r
      
      
      #检查配置是否生效
      exportfs
      ```

   2. 配置nfs-client（选做）

      ```shell
      #在node 节点执行
      showmount -e 172.16.0.115	#主节点内网IP
      
      mkdir -p /nfs/data
      
      mount -t nfs 172.16.0.115:/nfs/data /nfs/data	#主节点内网IP
      ```

   3. 配置默认存储

      > 配置动态供应的默认存储类

      ```sh
      ## 创建了一个存储类
      apiVersion: storage.k8s.io/v1
      kind: StorageClass
      metadata:
        name: nfs-storage
        annotations:
          storageclass.kubernetes.io/is-default-class: "true"
      provisioner: k8s-sigs.io/nfs-subdir-external-provisioner
      parameters:
        archiveOnDelete: "true"  ## 删除pv的时候，pv的内容是否要备份
      
      ---
      apiVersion: apps/v1
      kind: Deployment
      metadata:
        name: nfs-client-provisioner
        labels:
          app: nfs-client-provisioner
        # replace with namespace where provisioner is deployed
        namespace: default
      spec:
        replicas: 1
        strategy:
          type: Recreate
        selector:
          matchLabels:
            app: nfs-client-provisioner
        template:
          metadata:
            labels:
              app: nfs-client-provisioner
          spec:
            serviceAccountName: nfs-client-provisioner
            containers:
              - name: nfs-client-provisioner
                image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/nfs-subdir-external-provisioner:v4.0.2
                # resources:
                #    limits:
                #      cpu: 10m
                #    requests:
                #      cpu: 10m
                volumeMounts:
                  - name: nfs-client-root
                    mountPath: /persistentvolumes
                env:
                  - name: PROVISIONER_NAME
                    value: k8s-sigs.io/nfs-subdir-external-provisioner
                  - name: NFS_SERVER
                    value: 172.16.0.115 ## 指定自己nfs服务器地址
                  - name: NFS_PATH  
                    value: /nfs/data  ## nfs服务器共享的目录
            volumes:
              - name: nfs-client-root
                nfs:
                  server: 172.16.0.115	## 指定自己nfs服务器地址
                  path: /nfs/data	## nfs服务器共享的目录
      ---
      apiVersion: v1
      kind: ServiceAccount
      metadata:
        name: nfs-client-provisioner
        # replace with namespace where provisioner is deployed
        namespace: default
      ---
      kind: ClusterRole
      apiVersion: rbac.authorization.k8s.io/v1
      metadata:
        name: nfs-client-provisioner-runner
      rules:
        - apiGroups: [""]
          resources: ["nodes"]
          verbs: ["get", "list", "watch"]
        - apiGroups: [""]
          resources: ["persistentvolumes"]
          verbs: ["get", "list", "watch", "create", "delete"]
        - apiGroups: [""]
          resources: ["persistentvolumeclaims"]
          verbs: ["get", "list", "watch", "update"]
        - apiGroups: ["storage.k8s.io"]
          resources: ["storageclasses"]
          verbs: ["get", "list", "watch"]
        - apiGroups: [""]
          resources: ["events"]
          verbs: ["create", "update", "patch"]
      ---
      kind: ClusterRoleBinding
      apiVersion: rbac.authorization.k8s.io/v1
      metadata:
        name: run-nfs-client-provisioner
      subjects:
        - kind: ServiceAccount
          name: nfs-client-provisioner
          # replace with namespace where provisioner is deployed
          namespace: default
      roleRef:
        kind: ClusterRole
        name: nfs-client-provisioner-runner
        apiGroup: rbac.authorization.k8s.io
      ---
      kind: Role
      apiVersion: rbac.authorization.k8s.io/v1
      metadata:
        name: leader-locking-nfs-client-provisioner
        # replace with namespace where provisioner is deployed
        namespace: default
      rules:
        - apiGroups: [""]
          resources: ["endpoints"]
          verbs: ["get", "list", "watch", "create", "update", "patch"]
      ---
      kind: RoleBinding
      apiVersion: rbac.authorization.k8s.io/v1
      metadata:
        name: leader-locking-nfs-client-provisioner
        # replace with namespace where provisioner is deployed
        namespace: default
      subjects:
        - kind: ServiceAccount
          name: nfs-client-provisioner
          # replace with namespace where provisioner is deployed
          namespace: default
      roleRef:
        kind: Role
        name: leader-locking-nfs-client-provisioner
        apiGroup: rbac.authorization.k8s.io
      ```

      ```sh
      #执行上面的配置集，并确认配置是否生效
      [root@iZ7xv6wlrgwsz84rwpu5r8Z ~]# kubectl get sc
      NAME                    PROVISIONER                                   RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
      nfs-storage (default)   k8s-sigs.io/nfs-subdir-external-provisioner   Delete          Immediate           false                  8s
      ```

2. metrics-server

   > 集群指标监控组件
   
   ```yaml
   apiVersion: v1
   kind: ServiceAccount
   metadata:
     labels:
       k8s-app: metrics-server
     name: metrics-server
     namespace: kube-system
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: ClusterRole
   metadata:
     labels:
       k8s-app: metrics-server
       rbac.authorization.k8s.io/aggregate-to-admin: "true"
       rbac.authorization.k8s.io/aggregate-to-edit: "true"
       rbac.authorization.k8s.io/aggregate-to-view: "true"
     name: system:aggregated-metrics-reader
   rules:
   - apiGroups:
     - metrics.k8s.io
     resources:
     - pods
     - nodes
     verbs:
     - get
     - list
     - watch
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: ClusterRole
   metadata:
     labels:
       k8s-app: metrics-server
     name: system:metrics-server
   rules:
   - apiGroups:
     - ""
     resources:
     - pods
     - nodes
     - nodes/stats
     - namespaces
     - configmaps
     verbs:
     - get
     - list
     - watch
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: RoleBinding
   metadata:
     labels:
       k8s-app: metrics-server
     name: metrics-server-auth-reader
     namespace: kube-system
   roleRef:
     apiGroup: rbac.authorization.k8s.io
     kind: Role
     name: extension-apiserver-authentication-reader
   subjects:
   - kind: ServiceAccount
     name: metrics-server
     namespace: kube-system
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: ClusterRoleBinding
   metadata:
     labels:
       k8s-app: metrics-server
     name: metrics-server:system:auth-delegator
   roleRef:
     apiGroup: rbac.authorization.k8s.io
     kind: ClusterRole
     name: system:auth-delegator
   subjects:
   - kind: ServiceAccount
     name: metrics-server
     namespace: kube-system
   ---
   apiVersion: rbac.authorization.k8s.io/v1
   kind: ClusterRoleBinding
   metadata:
     labels:
       k8s-app: metrics-server
     name: system:metrics-server
   roleRef:
     apiGroup: rbac.authorization.k8s.io
     kind: ClusterRole
     name: system:metrics-server
   subjects:
   - kind: ServiceAccount
     name: metrics-server
     namespace: kube-system
   ---
   apiVersion: v1
   kind: Service
   metadata:
     labels:
       k8s-app: metrics-server
     name: metrics-server
     namespace: kube-system
   spec:
     ports:
     - name: https
       port: 443
       protocol: TCP
       targetPort: https
     selector:
       k8s-app: metrics-server
   ---
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     labels:
       k8s-app: metrics-server
     name: metrics-server
     namespace: kube-system
   spec:
     selector:
       matchLabels:
         k8s-app: metrics-server
     strategy:
       rollingUpdate:
         maxUnavailable: 0
     template:
       metadata:
         labels:
           k8s-app: metrics-server
       spec:
         containers:
         - args:
           - --cert-dir=/tmp
           - --kubelet-insecure-tls
           - --secure-port=4443
           - --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname
           - --kubelet-use-node-status-port
           image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/metrics-server:v0.4.3
           imagePullPolicy: IfNotPresent
           livenessProbe:
             failureThreshold: 3
             httpGet:
               path: /livez
               port: https
               scheme: HTTPS
             periodSeconds: 10
           name: metrics-server
           ports:
           - containerPort: 4443
             name: https
             protocol: TCP
           readinessProbe:
             failureThreshold: 3
             httpGet:
               path: /readyz
               port: https
               scheme: HTTPS
             periodSeconds: 10
           securityContext:
             readOnlyRootFilesystem: true
             runAsNonRoot: true
             runAsUser: 1000
           volumeMounts:
           - mountPath: /tmp
             name: tmp-dir
         nodeSelector:
           kubernetes.io/os: linux
         priorityClassName: system-cluster-critical
         serviceAccountName: metrics-server
         volumes:
         - emptyDir: {}
           name: tmp-dir
   ---
   apiVersion: apiregistration.k8s.io/v1
   kind: APIService
   metadata:
     labels:
       k8s-app: metrics-server
     name: v1beta1.metrics.k8s.io
   spec:
     group: metrics.k8s.io
     groupPriorityMinimum: 100
     insecureSkipTLSVerify: true
     service:
       name: metrics-server
       namespace: kube-system
     version: v1beta1
     versionPriority: 100
   ```
   
   ```sh
   #执行完上面的资源集后，使用以下命令测试是否成功
   [root@master ~]# kubectl top nodes
   NAME     CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%   
   master   238m         5%     2339Mi          31%       
   node1    1330m        16%    4046Mi          26%       
   node2    329m         4%     3915Mi          25%  
   kubectl top pods -A
   ```
#### 安装KubeSphere

1. 下载核心文件

   > 如果下载不到，请复制附录的内容

   ```sh
   wget https://github.com/kubesphere/ks-installer/releases/download/v3.2.0/kubesphere-installer.yaml
      
   wget https://github.com/kubesphere/ks-installer/releases/download/v3.2.0/cluster-configuration.yaml
   ```

2. 修改`cluster-configuration`

   > 在 `cluster-configuration.yaml`中指定我们需要开启的功能,具体可参考下面的附录文件
   >
   > 参照官网“启用可插拔组件” 
   >
   > https://kubesphere.com.cn/docs/pluggable-components/overview/

3. 执行安装

   ```sh
   kubectl apply -f kubesphere-installer.yaml
   
   kubectl apply -f cluster-configuration.yaml
   ```

4. 查看安装进度

   ```sh
   kubectl logs -n kubesphere-system $(kubectl get pod -n kubesphere-system -l app=ks-install -o jsonpath='{.items[0].metadata.name}') -f
   **************************************************
   Waiting for all tasks to be completed ...
   task network status is successful  (1/11)
   task alerting status is successful  (2/11)
   task multicluster status is successful  (3/11)
   task openpitrix status is successful  (4/11)
   task auditing status is successful  (5/11)
   task logging status is successful  (6/11)
   task events status is successful  (7/11)
   task kubeedge status is successful  (8/11)
   task devops status is successful  (9/11)
   task monitoring status is successful  (10/11)
   task servicemesh status is successful  (11/11)
   **************************************************
   Collecting installation results ...
   #####################################################
   ###              Welcome to KubeSphere!           ###
   #####################################################
   
   Console: http://172.16.0.115:30880
   Account: admin
   Password: P@88w0rd
   
   NOTES：
     1. After you log into the console, please check the
        monitoring status of service components in
        "Cluster Management". If any service is not
        ready, please wait patiently until all components 
        are up and running.
     2. Please change the default password after login.
   
   #####################################################
   https://kubesphere.io             2021-12-08 09:52:44
   #####################################################
   ```

   > 访问任意机器的 `30880`端口
   >
   > 账号 ： `admin`
   >
   > 密码 ： `P@88w0rd`

   > 解决`etcd`监控证书找不到问题
   >
   > ```sh
   > kubectl -n kubesphere-monitoring-system create secret generic kube-etcd-client-certs  --from-file=etcd-client-ca.crt=/etc/kubernetes/pki/etcd/ca.crt  --from-file=etcd-client.crt=/etc/kubernetes/pki/apiserver-etcd-client.crt  --from-file=etcd-client.key=/etc/kubernetes/pki/apiserver-etcd-client.key
   > ```

5. 附录

   - `kubesphere-installer.yaml`

     ```yaml
     ---
     apiVersion: apiextensions.k8s.io/v1
     kind: CustomResourceDefinition
     metadata:
       name: clusterconfigurations.installer.kubesphere.io
     spec:
       group: installer.kubesphere.io
       versions:
         - name: v1alpha1
           served: true
           storage: true
           schema:
             openAPIV3Schema:
               type: object
               properties:
                 spec:
                   type: object
                   x-kubernetes-preserve-unknown-fields: true
                 status:
                   type: object
                   x-kubernetes-preserve-unknown-fields: true
       scope: Namespaced
       names:
         plural: clusterconfigurations
         singular: clusterconfiguration
         kind: ClusterConfiguration
         shortNames:
           - cc
     
     ---
     apiVersion: v1
     kind: Namespace
     metadata:
       name: kubesphere-system
     
     ---
     apiVersion: v1
     kind: ServiceAccount
     metadata:
       name: ks-installer
       namespace: kubesphere-system
     
     ---
     apiVersion: rbac.authorization.k8s.io/v1
     kind: ClusterRole
     metadata:
       name: ks-installer
     rules:
     - apiGroups:
       - ""
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - apps
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - extensions
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - batch
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - rbac.authorization.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - apiregistration.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - apiextensions.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - tenant.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - certificates.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - devops.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - monitoring.coreos.com
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - logging.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - jaegertracing.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - storage.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - admissionregistration.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - policy
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - autoscaling
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - networking.istio.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - config.istio.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - iam.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - notification.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - auditing.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - events.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - core.kubefed.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - installer.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - storage.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - security.istio.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - monitoring.kiali.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - kiali.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - networking.k8s.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - kubeedge.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - types.kubefed.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - monitoring.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     - apiGroups:
       - application.kubesphere.io
       resources:
       - '*'
       verbs:
       - '*'
     
     
     ---
     kind: ClusterRoleBinding
     apiVersion: rbac.authorization.k8s.io/v1
     metadata:
       name: ks-installer
     subjects:
     - kind: ServiceAccount
       name: ks-installer
       namespace: kubesphere-system
     roleRef:
       kind: ClusterRole
       name: ks-installer
       apiGroup: rbac.authorization.k8s.io
     
     ---
     apiVersion: apps/v1
     kind: Deployment
     metadata:
       name: ks-installer
       namespace: kubesphere-system
       labels:
         app: ks-install
     spec:
       replicas: 1
       selector:
         matchLabels:
           app: ks-install
       template:
         metadata:
           labels:
             app: ks-install
         spec:
           serviceAccountName: ks-installer
           containers:
           - name: installer
             image: kubesphere/ks-installer:v3.2.0
             imagePullPolicy: "Always"
             resources:
               limits:
                 cpu: "1"
                 memory: 1Gi
               requests:
                 cpu: 20m
                 memory: 100Mi
             volumeMounts:
             - mountPath: /etc/localtime
               name: host-time
               readOnly: true
           volumes:
           - hostPath:
               path: /etc/localtime
               type: ""
             name: host-time
     ```

   - `cluster-configuration.yaml`

     ```yaml
     ---
     apiVersion: installer.kubesphere.io/v1alpha1
     kind: ClusterConfiguration
     metadata:
       name: ks-installer
       namespace: kubesphere-system
       labels:
         version: v3.2.0
     spec:
       persistence:
         storageClass: ""        # If there is no default StorageClass in your cluster, you need to specify an existing StorageClass here.
       authentication:
         jwtSecret: ""           # Keep the jwtSecret consistent with the Host Cluster. Retrieve the jwtSecret by executing "kubectl -n kubesphere-system get cm kubesphere-config -o yaml | grep -v "apiVersion" | grep jwtSecret" on the Host Cluster.
       local_registry: ""        # Add your private registry address if it is needed.
       # dev_tag: ""               # Add your kubesphere image tag you want to install, by default it's same as ks-install release version.
       etcd:
         monitoring: true       # Enable or disable etcd monitoring dashboard installation. You have to create a Secret for etcd before you enable it.
         endpointIps: 172.16.0.115  # etcd cluster EndpointIps. It can be a bunch of IPs here.
         port: 2379              # etcd port.
         tlsEnable: true
       common:
         core:
           console:
             enableMultiLogin: true  # Enable or disable simultaneous logins. It allows different users to log in with the same account at the same time.
             port: 30880
             type: NodePort
         # apiserver:            # Enlarge the apiserver and controller manager's resource requests and limits for the large cluster
         #  resources: {}
         # controllerManager:
         #  resources: {}
         redis:
           enabled: true
           volumeSize: 2Gi # Redis PVC size.
         openldap:
           enabled: true
           volumeSize: 2Gi   # openldap PVC size.
         minio:
           volumeSize: 20Gi # Minio PVC size.
         monitoring:
           # type: external   # Whether to specify the external prometheus stack, and need to modify the endpoint at the next line.
           endpoint: http://prometheus-operated.kubesphere-monitoring-system.svc:9090 # Prometheus endpoint to get metrics data.
           GPUMonitoring:     # Enable or disable the GPU-related metrics. If you enable this switch but have no GPU resources, Kubesphere will set it to zero. 
             enabled: false
         gpu:                 # Install GPUKinds. The default GPU kind is nvidia.com/gpu. Other GPU kinds can be added here according to your needs. 
           kinds:         
           - resourceName: "nvidia.com/gpu"
             resourceType: "GPU"
             default: true
         es:   # Storage backend for logging, events and auditing.
           # master:
           #   volumeSize: 4Gi  # The volume size of Elasticsearch master nodes.
           #   replicas: 1      # The total number of master nodes. Even numbers are not allowed.
           #   resources: {}
           # data:
           #   volumeSize: 20Gi  # The volume size of Elasticsearch data nodes.
           #   replicas: 1       # The total number of data nodes.
           #   resources: {}
           logMaxAge: 7             # Log retention time in built-in Elasticsearch. It is 7 days by default.
           elkPrefix: logstash      # The string making up index names. The index name will be formatted as ks-<elk_prefix>-log.
           basicAuth:
             enabled: false
             username: ""
             password: ""
           externalElasticsearchUrl: ""
           externalElasticsearchPort: ""
       alerting:                # (CPU: 0.1 Core, Memory: 100 MiB) It enables users to customize alerting policies to send messages to receivers in time with different time intervals and alerting levels to choose from.
         enabled: true         # Enable or disable the KubeSphere Alerting System.
         # thanosruler:
         #   replicas: 1
         #   resources: {}
       auditing:                # Provide a security-relevant chronological set of records，recording the sequence of activities happening on the platform, initiated by different tenants.
         enabled: true         # Enable or disable the KubeSphere Auditing Log System.
         # operator:
         #   resources: {}
         # webhook:
         #   resources: {}
       devops:                  # (CPU: 0.47 Core, Memory: 8.6 G) Provide an out-of-the-box CI/CD system based on Jenkins, and automated workflow tools including Source-to-Image & Binary-to-Image.
         enabled: true             # Enable or disable the KubeSphere DevOps System.
         # resources: {}
         jenkinsMemoryLim: 2Gi      # Jenkins memory limit.
         jenkinsMemoryReq: 1500Mi   # Jenkins memory request.
         jenkinsVolumeSize: 8Gi     # Jenkins volume size.
         jenkinsJavaOpts_Xms: 512m  # The following three fields are JVM parameters.
         jenkinsJavaOpts_Xmx: 512m
         jenkinsJavaOpts_MaxRAM: 2g
       events:                  # Provide a graphical web console for Kubernetes Events exporting, filtering and alerting in multi-tenant Kubernetes clusters.
         enabled: true         # Enable or disable the KubeSphere Events System.
         # operator:
         #   resources: {}
         # exporter:
         #   resources: {}
         # ruler:
         #   enabled: true
         #   replicas: 2
         #   resources: {}
       logging:                 # (CPU: 57 m, Memory: 2.76 G) Flexible logging functions are provided for log query, collection and management in a unified console. Additional log collectors can be added, such as Elasticsearch, Kafka and Fluentd.
         enabled: true         # Enable or disable the KubeSphere Logging System.
         containerruntime: docker
         logsidecar:
           enabled: true
           replicas: 2
           # resources: {}
       metrics_server:                    # (CPU: 56 m, Memory: 44.35 MiB) It enables HPA (Horizontal Pod Autoscaler).
         enabled: false                   # Enable or disable metrics-server.
       monitoring:
         storageClass: ""                 # If there is an independent StorageClass you need for Prometheus, you can specify it here. The default StorageClass is used by default.
         # kube_rbac_proxy:
         #   resources: {}
         # kube_state_metrics:
         #   resources: {}
         # prometheus:
         #   replicas: 1  # Prometheus replicas are responsible for monitoring different segments of data source and providing high availability.
         #   volumeSize: 20Gi  # Prometheus PVC size.
         #   resources: {}
         #   operator:
         #     resources: {}
         #   adapter:
         #     resources: {}
         # node_exporter:
         #   resources: {}
         # alertmanager:
         #   replicas: 1          # AlertManager Replicas.
         #   resources: {}
         # notification_manager:
         #   resources: {}
         #   operator:
         #     resources: {}
         #   proxy:
         #     resources: {}
         gpu:                           # GPU monitoring-related plugins installation.
           nvidia_dcgm_exporter:
             enabled: false
             # resources: {}
       multicluster:
         clusterRole: none  # host | member | none  # You can install a solo cluster, or specify it as the Host or Member Cluster.
       network:
         networkpolicy: # Network policies allow network isolation within the same cluster, which means firewalls can be set up between certain instances (Pods).
           # Make sure that the CNI network plugin used by the cluster supports NetworkPolicy. There are a number of CNI network plugins that support NetworkPolicy, including Calico, Cilium, Kube-router, Romana and Weave Net.
           enabled: true # Enable or disable network policies.
         ippool: # Use Pod IP Pools to manage the Pod network address space. Pods to be created can be assigned IP addresses from a Pod IP Pool.
           type: none # Specify "calico" for this field if Calico is used as your CNI plugin. "none" means that Pod IP Pools are disabled.
         topology: # Use Service Topology to view Service-to-Service communication based on Weave Scope.
           type: none # Specify "weave-scope" for this field to enable Service Topology. "none" means that Service Topology is disabled.
       openpitrix: # An App Store that is accessible to all platform tenants. You can use it to manage apps across their entire lifecycle.
         store:
           enabled: true # Enable or disable the KubeSphere App Store.
       servicemesh:         # (0.3 Core, 300 MiB) Provide fine-grained traffic management, observability and tracing, and visualized traffic topology.
         enabled: true     # Base component (pilot). Enable or disable KubeSphere Service Mesh (Istio-based).
       kubeedge:          # Add edge nodes to your cluster and deploy workloads on edge nodes.
         enabled: true   # Enable or disable KubeEdge.
         cloudCore:
           nodeSelector: {"node-role.kubernetes.io/worker": ""}
           tolerations: []
           cloudhubPort: "10000"
           cloudhubQuicPort: "10001"
           cloudhubHttpsPort: "10002"
           cloudstreamPort: "10003"
           tunnelPort: "10004"
           cloudHub:
             advertiseAddress: # At least a public IP address or an IP address which can be accessed by edge nodes must be provided.
               - ""            # Note that once KubeEdge is enabled, CloudCore will malfunction if the address is not provided.
             nodeLimit: "100"
           service:
             cloudhubNodePort: "30000"
             cloudhubQuicNodePort: "30001"
             cloudhubHttpsNodePort: "30002"
             cloudstreamNodePort: "30003"
             tunnelNodePort: "30004"
         edgeWatcher:
           nodeSelector: {"node-role.kubernetes.io/worker": ""}
           tolerations: []
           edgeWatcherAgent:
             nodeSelector: {"node-role.kubernetes.io/worker": ""}
             tolerations: []
     ```

## Linux单节点部署KubeSphere

1. 开通服务器,4c8g；centos7.9；防火墙放行  30000~32767；指定`hostname`

   ```sh
   hostnamectl set-hostname master
   ```

2. 准备KubeKey

   ```sh
   export KKZONE=cn
   
   curl -sfL https://get-kk.kubesphere.io | VERSION=v1.2.0 sh -
   
   chmod +x kk
   ```

3. 使用KubeKey引导安装集群
   
   ```sh
   #可能需要下面命令
   yum install -y conntrack
   
   ./kk create cluster --with-kubernetes v1.21.5 --with-kubesphere v3.2.0
   ```
   
4. 安装后默认是最小安装，若要开启其它功能
   
   > 平台管理 -->自定义资源CRD --> 修改`clusterconfiguration`配置，此配置文件同上附录文件`cluster-configuration.yaml`
   
## Linux多节点部署KubeSphere

1. 准备三台服务器

   - 4c8g （master）
   - 8c16g * 2（worker）
   - centos7.9
   - 内网互通
   - 每个机器有自己域名
   - 防火墙开放30000~32767端口
   
1. 各个节点配置`hostname`

   ```sh
   hostnamectl set-hostname ****
   ```
   
3. 下载KubeKey

   ```sh
   export KKZONE=cn
   
   curl -sfL https://get-kk.kubesphere.io | VERSION=v1.1.1 sh -
   
   chmod +x kk
   ```

4. 创建集群配置文件

   ```sh
   #可能需要下面命令
   yum install -y conntrack
   
   ./kk create config --with-kubernetes v1.20.4 --with-kubesphere v3.1.1
   ```

4. 创建集群

   ```sh
   ./kk create cluster -f config-sample.yaml
   ```

5. 查看进度

   ```sh
   kubectl logs -n kubesphere-system $(kubectl get pod -n kubesphere-system -l app=ks-install -o jsonpath='{.items[0].metadata.name}') -f
   ```

6. 附录

   - `config-sample.yaml`示例文件

     ```yaml
     apiVersion: kubekey.kubesphere.io/v1alpha1
     kind: Cluster
     metadata:
       name: sample
     spec:
       hosts:
       - {name: master, address: 172.16.0.115, internalAddress: 172.16.0.115, user: root, password: 123456}
       - {name: node1, address: 172.16.0.117, internalAddress: 172.16.0.117, user: root, password: 123456}
       - {name: node2, address: 172.16.0.116, internalAddress: 172.16.0.116, user: root, password: 123456}
       roleGroups:
         etcd:
         - master
         master: 
         - master
         worker:
         - node1
         - node2
       controlPlaneEndpoint:
         domain: lb.kubesphere.local
         address: ""
         port: 6443
       kubernetes:
         version: v1.20.4
         imageRepo: kubesphere
         clusterName: cluster.local
       network:
         plugin: calico
         kubePodsCIDR: 10.233.64.0/18
         kubeServiceCIDR: 10.233.0.0/18
       registry:
         registryMirrors: []
         insecureRegistries: []
       addons: []
     
     
     ---
     apiVersion: installer.kubesphere.io/v1alpha1
     kind: ClusterConfiguration
     metadata:
       name: ks-installer
       namespace: kubesphere-system
       labels:
         version: v3.1.1
     spec:
       persistence:
         storageClass: ""       
       authentication:
         jwtSecret: ""
       zone: ""
       local_registry: ""        
       etcd:
         monitoring: true      
         endpointIps: localhost  
         port: 2379             
         tlsEnable: true
       common:
         redis:
           enabled: true
         redisVolumSize: 2Gi 
         openldap:
           enabled: true
         openldapVolumeSize: 2Gi  
         minioVolumeSize: 20Gi
         monitoring:
           endpoint: http://prometheus-operated.kubesphere-monitoring-system.svc:9090
         es:  
           elasticsearchMasterVolumeSize: 4Gi   
           elasticsearchDataVolumeSize: 20Gi   
           logMaxAge: 7          
           elkPrefix: logstash
           basicAuth:
             enabled: false
             username: ""
             password: ""
           externalElasticsearchUrl: ""
           externalElasticsearchPort: ""  
       console:
         enableMultiLogin: true 
         port: 30880
       alerting:       
         enabled: false
         # thanosruler:
         #   replicas: 1
         #   resources: {}
       auditing:    
         enabled: false
       devops:           
         enabled: true
         jenkinsMemoryLim: 2Gi     
         jenkinsMemoryReq: 1500Mi 
         jenkinsVolumeSize: 8Gi   
         jenkinsJavaOpts_Xms: 512m  
         jenkinsJavaOpts_Xmx: 512m
         jenkinsJavaOpts_MaxRAM: 2g
       events:          
         enabled: true
         ruler:
           enabled: true
           replicas: 2
       logging:         
         enabled: true
         logsidecar:
           enabled: true
           replicas: 2
       metrics_server:             
         enabled: true
       monitoring:
         storageClass: ""
         prometheusMemoryRequest: 400Mi  
         prometheusVolumeSize: 20Gi  
       multicluster:
         clusterRole: none 
       network:
         networkpolicy:
           enabled: false
         ippool:
           type: none
         topology:
           type: none
       openpitrix:
         store:
           enabled: true
       servicemesh:    
         enabled: true  
       kubeedge:
         enabled: true
         cloudCore:
           nodeSelector: {"node-role.kubernetes.io/worker": ""}
           tolerations: []
           cloudhubPort: "10000"
           cloudhubQuicPort: "10001"
           cloudhubHttpsPort: "10002"
           cloudstreamPort: "10003"
           tunnelPort: "10004"
           cloudHub:
             advertiseAddress: 
               - ""           
             nodeLimit: "100"
           service:
             cloudhubNodePort: "30000"
             cloudhubQuicNodePort: "30001"
             cloudhubHttpsNodePort: "30002"
             cloudstreamNodePort: "30003"
             tunnelNodePort: "30004"
         edgeWatcher:
           nodeSelector: {"node-role.kubernetes.io/worker": ""}
           tolerations: []
           edgeWatcherAgent:
             nodeSelector: {"node-role.kubernetes.io/worker": ""}
             tolerations: []
     ```

# KubeSphere实战

## 中间件部署实战

### 部署MySQL

1. mysql容器启动

   ```sh
   docker run -p 3306:3306 --name mysql-01 \
   -v /mydata/mysql/log:/var/log/mysql \
   -v /mydata/mysql/data:/var/lib/mysql \
   -v /mydata/mysql/conf:/etc/mysql/conf.d \
   -e MYSQL_ROOT_PASSWORD=root \
   --restart=always \
   -d mysql:5.7 
   ```

   > 根据上面docker执行的命令
   >
   > - 环境变量
   >
   >   ```
   >   key:MYSQL_ROOT_PASSWORD
   >   value:123456
   >   ```
   >
   > - 数据挂载路径，路径如下
   >
   >   ```sh
   >   /var/lib/mysql
   >   ```
   >
   > - 配置文件挂载,路径如下
   >
   >   ```sh
   >   /etc/mysql/conf.d
   >   ```

2. mysql配置示例,`my.cnf`

   ```
   [client]
   default-character-set=utf8mb4
    
   [mysql]
   default-character-set=utf8mb4
    
   [mysqld]
   init_connect='SET collation_connection = utf8mb4_unicode_ci'
   init_connect='SET NAMES utf8mb4'
   character-set-server=utf8mb4
   collation-server=utf8mb4_unicode_ci
   skip-character-set-client-handshake
   skip-name-resolve
   ```

2. 成功部署`mysql`

   ```
   [root@blank ~]# kubectl get ns
   NAME                              STATUS   AGE
   default                           Active   53m
   kube-node-lease                   Active   53m
   kube-public                       Active   53m
   kube-system                       Active   53m
   kubesphere-controls-system        Active   52m
   kubesphere-monitoring-federated   Active   52m
   kubesphere-monitoring-system      Active   53m
   kubesphere-system                 Active   53m
   yst                               Active   34m
   You have new mail in /var/spool/mail/root
   [root@blank ~]# kubectl get all -n yst
   NAME              READY   STATUS    RESTARTS   AGE
   pod/yst-mysql-0   1/1     Running   0          14m
   
   NAME                     TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)              AGE
   service/yst-mysql-gw61   ClusterIP   None         <none>        3306/TCP,33060/TCP   14m
   
   NAME                         READY   AGE
   statefulset.apps/yst-mysql   1/1     14m
   ```
   
3. 部署成功 ，在`service `下会生成默认DNS可供内网进行访问
   
   ```sh
   # mysql -uroot -hyst-mysql-gw61.yst -p
   ```
   
   > 1、集群内部，直接通过应用的  【服务名.项目名】 直接访问 ,默认的DNS可以更改如下
   >
   > ​        mysql -uroot **-hyst-mysql.yst** -p 
   >
   > 2、集群外部，
   

### 部署Redis

1. `docker ``redis`容器启动

   ```sh
   #创建配置文件
   ## 1、准备redis配置文件内容
   mkdir -p /mydata/redis/conf && vim /mydata/redis/conf/redis.conf
   
   ##配置示例
   appendonly yes
   port 6379
   bind 0.0.0.0
   
   #docker启动redis
   docker run -d -p 6379:6379 --restart=always \
   -v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
   -v  /mydata/redis-01/data:/data \
    --name redis-01 redis:6.2.5 \
    redis-server /etc/redis/redis.conf
   ```
   
   > 根据上面启动的docker启动的命令
   >
   > - 启动参数
   >
   >   ```sh
   >   key: redis-server
   >   value: /etc/redis/redis.conf
   >   ```
   >
   > - 数据持久化挂载
   >
   >   ```sh
   >   /data
   >   ```
   >
   > - 配置挂载
   >
   >   ```sh
   >   /etc/redis/redis.conf
   >   ```

### 部署ElasticSearch

1. es容器启动

   ```sh
   # 创建数据目录
   mkdir -p /mydata/es-01 && chmod 777 -R /mydata/es-01
   
   # 容器启动
   docker run --restart=always -d -p 9200:9200 -p 9300:9300 \
   -e "discovery.type=single-node" \
   -e ES_JAVA_OPTS="-Xms512m -Xmx512m" \
   -v es-config:/usr/share/elasticsearch/config \
   -v /mydata/es-01/data:/usr/share/elasticsearch/data \
   --name es-01 \
   elasticsearch:7.13.4
   ```

   > 根据上面的配置
   >
   > - 环境变量
   >
   >   ```
   >   key : ES_JAVA_OPTS
   >   value: -Xms512m -Xmx512m
   >   ```
   >
   > - 数据挂载
   >
   >   ```sh
   >   /usr/share/elasticsearch/data
   >   ```
   >
   > - 配置文件挂载(**注意：这里因为es里的配置文件比较多，如果按上面的配置路径挂载，会把所有的不必要的文件都覆盖，因为这里只需要指定子目录，举例:只挂载2个文件**)
   >
   >   ```sh
   >   /usr/share/elasticsearch/config/jvm.options
   >   /usr/share/elasticsearch/config/elasticsearch.yml
   >   ```
   
   > 注意： 子路径挂载，配置修改后，k8s不会对其Pod内的相关配置文件进行热更新，需要自己重启Pod

### 应用商店

### 应用仓库

## RuoYi-Cloud部署实战

- 每个微服务准备 `bootstrap.properties`，配置 `nacos`地址信息。默认使用本地

- 每个微服务准备`Dockerfile`，启动命令，指定线上`nacos`配置等。

- 每个微服务制作自己镜像。

- 项目架构

  ```
  com.ruoyi     
  ├── ruoyi-ui              // 前端框架 [80]
  ├── ruoyi-gateway         // 网关模块 [8080]
  ├── ruoyi-auth            // 认证中心 [9200]
  ├── ruoyi-api             // 接口模块
  │       └── ruoyi-api-system                          // 系统接口
  ├── ruoyi-common          // 通用模块
  │       └── ruoyi-common-core                         // 核心模块
  │       └── ruoyi-common-datascope                    // 权限范围
  │       └── ruoyi-common-datasource                   // 多数据源
  │       └── ruoyi-common-log                          // 日志记录
  │       └── ruoyi-common-redis                        // 缓存服务
  │       └── ruoyi-common-security                     // 安全模块
  │       └── ruoyi-common-swagger                      // 系统接口
  ├── ruoyi-modules         // 业务模块
  │       └── ruoyi-system                              // 系统模块 [9201]
  │       └── ruoyi-gen                                 // 代码生成 [9202]
  │       └── ruoyi-job                                 // 定时任务 [9203]
  │       └── ruoyi-file                                // 文件服务 [9300]
  ├── ruoyi-visual          // 图形化管理模块
  │       └── ruoyi-visual-monitor                      // 监控中心 [9100]
  ├──pom.xml                // 公共依赖
  ```

1. Dockerfile文件

   ```dockerfile
   FROM openjdk:8-jdk
   LABEL maintainer=dxb02
   
   #docker run -e PARAMS="--server.port 9090"
   ENV PARAMS="--server.port=8080 --spring.profiles.active=prod --spring.cloud.nacos.discovery.server-addr=nacos.ruoyi:8848 --spring.cloud.nacos.config.server-addr=nacos.ruoyi:8848 --spring.cloud.nacos.config.namespace=prod --spring.cloud.nacos.config.file-extension=yml"
   RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
   
   COPY target/*.jar /app.jar
   EXPOSE 8080
   
   #
   ENTRYPOINT ["/bin/sh","-c","java -Dfile.encoding=utf8 -Djava.security.egd=file:/dev/./urandom -jar app.jar ${PARAMS}"]
   ```

   > 规则：
   >
   > 1、全部容器默认以8080端口启动（因各个pod会开启多个）
   >
   > 2、时间为CST
   >
   > 3、环境变量 PARAMS 可以动态指定配置文件中任意的值
   >
   > 4、nacos集群内地址为 ` nacos.ruoyi:8848 `
   >
   > 5、微服务默认启动加载 `nacos`中  ` 服务名-激活的环境.yml ` 文件，所以线上的配置可以全部写在`nacos`中。
   
2. 部署`nacos`

   hub.docker.com搜索`nacos/nacos-server:v2.0.3`

   > 注意：
   >
   > 因SpringCloud项目应用要等nacos启动成功才能获取其配置，不然在服务器重启重，项目容器一直重启直到nacos启动成功才能启动,因此在配置nacos时要加多个健康检查检测，**容器检查存活**
   >
   > ```
   > HTTP请求：HTTP /nacos 8848
   > ```
   >
   > 配置目录挂载`/home/nacos/conf`,下面文件必须要设置子路径
   >
   > - `application.properties` 文件
   >
   >   ```
   >   spring.datasource.platform=mysql
   >   db.num=1
   >   db.url.0=jdbc:mysql://mysql.ruoyi:3306/ry-config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
   >   db.user=root
   >   db.password=123456
   >   
   >   nacos.naming.empty-service.auto-clean=true
   >   nacos.naming.empty-service.clean.initial-delay-ms=50000
   >   nacos.naming.empty-service.clean.period-time-ms=30000
   >   
   >   management.endpoints.web.exposure.include=*
   >   
   >   management.metrics.export.elastic.enabled=false
   >   management.metrics.export.influx.enabled=false
   >   
   >   server.tomcat.accesslog.enabled=true
   >   server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D %{User-Agent}i %{Request-Source}i
   >   
   >   server.tomcat.basedir=
   >   
   >   nacos.security.ignore.urls=/,/error,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-ui/public/**,/v1/auth/**,/v1/console/health/**,/actuator/**,/v1/console/server/**
   >   
   >   nacos.core.auth.system.type=nacos
   >   nacos.core.auth.enabled=false
   >   nacos.core.auth.default.token.expire.seconds=18000
   >   nacos.core.auth.default.token.secret.key=SecretKey012345678901234567890123456789012345678901234567890123456789
   >   nacos.core.auth.caching.enabled=true
   >   nacos.core.auth.enable.userAgentAuthWhite=false
   >   nacos.core.auth.server.identity.key=serverIdentity
   >   nacos.core.auth.server.identity.value=security
   >   
   >   nacos.istio.mcp.server.enabled=false
   >   ```
   >
   >   > **注意：上面的的mysql地址是内网DNS映射地址**
   >
   > - `cluster.conf`
   >
   >   ```
   >   #
   >   # Copyright 1999-2018 Alibaba Group Holding Ltd.
   >   #
   >   # Licensed under the Apache License, Version 2.0 (the "License");
   >   # you may not use this file except in compliance with the License.
   >   # You may obtain a copy of the License at
   >   #
   >   #      http://www.apache.org/licenses/LICENSE-2.0
   >   #
   >   # Unless required by applicable law or agreed to in writing, software
   >   # distributed under the License is distributed on an "AS IS" BASIS,
   >   # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   >   # See the License for the specific language governing permissions and
   >   # limitations under the License.
   >   #
   >                   
   >   #it is ip
   >   #example
   >   nacos-v1-0.nacos.ruoyi.svc.cluster.local:：8848
   >   nacos-v1-1.nacos.ruoyi.svc.cluster.local：8848
   >   nacos-v1-2.nacos.ruoyi.svc.cluster.local：8848
   >   ```
   >
   >   > **注意：在部署项目时，因使用的是nacos版本2.x以上的，不支持此版本部署，因为在部署时要作调整为单机部署，至于2.x以上集群这里不作说明**
   >   >
   >   > **在部署naocs为单机模式时，添加环境参数添加`MODE=standalone`**
   
   当成功部署项目下的`nacos`后，可以通过`kubesphere`内的pod ping下集群内DNS以获取各节点地址，如下
   
   ```sh
   sh-4.2# ping nacos.ruoyi
   PING nacos.ruoyi.svc.cluster.local (10.233.96.49) 56(84) bytes of data.
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=1 ttl=64 time=0.014 ms
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=2 ttl=64 time=0.025 ms
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=3 ttl=64 time=0.021 ms
   
   sh-4.2# ping nacos-v1-2.nacos.ruoyi.svc.cluster.local
   PING nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49) 56(84) bytes of data.
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=1 ttl=64 time=0.023 ms
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=2 ttl=64 time=0.034 ms
   64 bytes from nacos-v1-2.nacos.ruoyi.svc.cluster.local (10.233.96.49): icmp_seq=3 ttl=64 time=0.033 ms
   
   --- nacos-v1-2.nacos.ruoyi.svc.cluster.local ping statistics ---
   3 packets transmitted, 3 received, 0% packet loss, time 1999ms
   rtt min/avg/max/mdev = 0.023/0.030/0.034/0.005 ms
   
   sh-4.2# ping nacos-v1-0.nacos.ruoyi.svc.cluster.local
   PING nacos-v1-0.nacos.ruoyi.svc.cluster.local (10.233.96.47) 56(84) bytes of data.
   64 bytes from nacos-v1-0.nacos.ruoyi.svc.cluster.local (10.233.96.47): icmp_seq=1 ttl=63 time=0.065 ms
   64 bytes from nacos-v1-0.nacos.ruoyi.svc.cluster.local (10.233.96.47): icmp_seq=2 ttl=63 time=0.052 ms
   64 bytes from nacos-v1-0.nacos.ruoyi.svc.cluster.local (10.233.96.47): icmp_seq=3 ttl=63 time=0.052 ms
   
   --- nacos-v1-0.nacos.ruoyi.svc.cluster.local ping statistics ---
   3 packets transmitted, 3 received, 0% packet loss, time 2001ms
   rtt min/avg/max/mdev = 0.052/0.056/0.065/0.008 ms
   
   ...
   ```
   
   > 如下，可以看到配置的`nacos`集群规则如下，根据`pods`数量递增改变x，x由0开始
   >
   > ```
   >  nacos-v1-x.nacos.ruoyi.svc.cluster.local
   > ```
   
3. 各中间件 地址
   
   ```
   redis.ruoyi:6379
   mysql.ruoyi:3306
   nacos.ruoyi:8848
   ```
   
3. 创建项目镜像
   
   - 创建
   
     ```sh
     docker build -t ruoyi-auth:v1 -f Dockerfile .
     ```
   
   - 推送镜像给阿里云
   
     ```sh
     $ docker login --username=李估唔到 registry.cn-guangzhou.aliyuncs.com
     #把本地镜像，改名，成符合阿里云名字规范的镜像。
     $ docker tag [ImageId] registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi:[镜像版本号]
     #docker tag  12300e0d25fe registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-auth:v1
     
     $ docker push registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi:[镜像版本号]
     #docker push registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-auth:v1
     ```
   
     > 因阿里推送规则，可直接创建适合推送的镜像名
     >
     > ```sh
     > docker build -t registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-ui:v1 -f dockerfile .
     > ```
   
5. `ruoyi`所有镜像,按照上面项目架构，由下至上部署，后端服务全部部署端口都为`8080`

   ```sh
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-monitor:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-file:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-job:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-gen:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-system:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-auth:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-gateway:v1
   docker pull registry.cn-guangzhou.aliyuncs.com/dx-ruoyi-program/ruoyi-ui:v1
   ```

6. 部署规则

   - **应用一启动会获取到 `应用名-激活的环境标识.yml`**
   - **每次部署应用的时候，需要提前修改nacos线上配置，确认好每个中间件的连接地址是否正确**

------

# 云原生DevOps基础与实战

1. DevOps简介

   DevOps **是一系列做法和工具**，可以使 IT 和软件开发团队之间的**流程实现自动化**。其中，随着敏捷软件开发日趋流行，**持续集成 (CI)** 和**持续交付 (CD)** 已经成为该领域一个理想的解决方案。在 CI/CD 工作流中，每次集成都通过自动化构建来验证，包括编码、发布和测试，从而帮助开发者提前发现集成错误，团队也可以快速、安全、可靠地将内部软件交付到生产环境。

   不过，传统的 Jenkins Controller-Agent 架构（即多个 Agent 为一个 Controller 工作）有以下不足。

   - 如果 Controller 宕机，整个 CI/CD 流水线会崩溃。
   - 资源分配不均衡，一些 Agent 的流水线任务 (Job) 出现排队等待，而其他 Agent 处于空闲状态。
   - 不同的 Agent 可能配置环境不同，并需要使用不同的编码语言。这种差异会给管理和维护带来不便。

## 尚医通项目上云

> 如果是以单节点最小安装方式，可使用以下方法启用`DevOps`
>
> https://kubesphere.io/zh/docs/pluggable-components/devops/#%E5%9C%A8%E5%AE%89%E8%A3%85%E5%90%8E%E5%90%AF%E7%94%A8-devops
>
> 以方式有异常无法开启，要重启安装
>
> ```
> kubectl rollout restart deploy/ks-installer -n kubesphere-system
> ```

1. 项目架构

   ```
   yygh-parent
   |---common                                  //通用模块
   |---hospital-manage                         //医院后台				[9999]   
   |---model									//数据模型
   |---server-gateway							//网关    				[80]
   |---service									//微服务层
   |-------service-cmn							//公共服务				[8202]
   |-------service-hosp						//医院数据服务		[8201]
   |-------service-order						//预约下单服务		[8206]
   |-------service-oss							//对象存储服务		[8205]
   |-------service-sms							//短信服务				[8204]
   |-------service-statistics					//统计服务				[8208]
   |-------service-task						//定时服务				[8207]
   |-------service-user						//会员服务				[8203]
   
   
   ====================================================================
   
   yygh-admin									//医院管理后台		[9528]
   yygh-site									/挂号平台				[3000]
   ```

2. 中间件

   | 中间件        | 集群内地址                       | 外部访问地址                                                 |
   | ------------- | -------------------------------- | ------------------------------------------------------------ |
   | Nacos         | nacos.yst:8848                   | [http://139.198.165.238:30349/](http://139.198.165.238/)nacos |
   | MySQL         | mysql.yst:3306                   | [139.198.165.238](http://139.198.165.238):31840              |
   | Redis         | redis.yst:6379                   | [139.198.165.238](http://139.198.165.238):31968              |
   | Sentinel      | sentinel.yst:8080                | http://139.198.165.238:31523/                                |
   | MongoDB       | mongodb.yst:**27017**            | [139.198.165.238](http://139.198.165.238)**:32693**          |
   | RabbitMQ      | rabbitm-yp1tx4-rabbitmq.yst:5672 | [139.198.165.238](http://139.198.165.238):30375              |
   | ElasticSearch | es.yst:9200                      | [139.198.165.238](http://139.198.165.238):31300              |

   > Sentinel docker:https://hub.docker.com/r/leifengyang/sentinel
   >
   > ```sh
   > docker pull leifengyang/sentinel:1.8.2
   > ```

3. 流水线

   1. 项目地址

      - https://gitee.com/leifengyang/yygh-parent
      - https://gitee.com/leifengyang/yygh-admin
      - https://gitee.com/leifengyang/yygh-site

   2. 项目默认规则

      - 每个微服务项目，在生产环境时，会自动获取   ` 微服务名-prod.yml ` 作为自己的核心配置文件
      - 每个微服务项目，在生产环境时，默认都是使用 `8080` 端口

   3. `DevOps`创建后端流水线部署(下例为示例,实际以应用开发为主) 

      1. 克隆元程代码

         ```
         #代理类型(none)->条件(无)->任务(1.指定容器base 2. 添加嵌套步骤git,要添加相应的私人仓库验证 3.添加嵌套步骤,添加shell脚本,克隆完可以使用 shell命令打印目录结构ls -al)
         ```
   
      2.  项目编译 
   
         > 当前是Java项目，要使用`mavne`进行编译打包
         >
         > 修改`maven`从阿里云下载镜像,配置中心->配置->搜索`ks-devops-agent`,添加阿里云加速镜像 
         >
         > 已经下载过的jar包，下一次流水线的启动，不会重复下载
         
         ```sh
         #代理类型(none)->条件(无)->任务(1.指定容器maven 2.在容器内添加嵌套步骤 执行ls -al看下当前路径 3. 在容器内添加嵌套步骤,再执行下面的命令进行打包) 4.进入hospital-manage/target检查是否打包成功 ls hospital-manage/target
         mvn clean package -Dmaven.test.skip=true
         ```
         
      3. 打包发布镜像
   
         ```
         #代理类型(none)->条件(无)->任务
         	1.指定容器maven 
         	2.在容器内添加嵌套步骤 执行ls -al看下当前路径 
         	3. 打包镜像
         		docker build -t hospital-manage:latest -f hospital-manage/Dockerfile ./hospital-manage/
         	4.查询是否镜像创建成功
         		docker iamges
         ```
   
      4. 编辑`Jenkinsfile`文件,把其它服务一并打包
   
         ```j
             stage('default-2') {
               parallel {
                 stage('构建hospital-manage镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./hospital-manage/target'
                       sh 'docker build -t hospital-manage:latest -f hospital-manage/Dockerfile  ./hospital-manage/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建server-gateway镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./server-gateway/target'
                       sh 'docker build -t server-gateway:latest -f server-gateway/Dockerfile  ./server-gateway/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-cmn镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-cmn/target'
                       sh 'docker build -t service-cmn:latest -f service/service-cmn/Dockerfile  ./service/service-cmn/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-hosp镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-hosp/target'
                       sh 'docker build -t service-hosp:latest -f service/service-hosp/Dockerfile  ./service/service-hosp/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-order镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-order/target'
                       sh 'docker build -t service-order:latest -f service/service-order/Dockerfile  ./service/service-order/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-oss镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-oss/target'
                       sh 'docker build -t service-oss:latest -f service/service-oss/Dockerfile  ./service/service-oss/'
                       sh 'docker images'
                     }
         
                   }
                 }
                 stage('构建service-sms镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-sms/target'
                       sh 'docker build -t service-sms:latest -f service/service-sms/Dockerfile  ./service/service-sms/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-statistics镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-statistics/target'
                       sh 'docker build -t service-statistics:latest -f service/service-statistics/Dockerfile  ./service/service-statistics/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-task镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-task/target'
                       sh 'docker build -t service-task:latest -f service/service-task/Dockerfile  ./service/service-task/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
                 stage('构建service-user镜像') {
                   agent none
                   steps {
                     container('maven') {
                       sh 'ls -al  ./service/service-user/target'
                       sh 'docker build -t service-user:latest -f service/service-user/Dockerfile  ./service/service-user/'
                       sh 'docker images'
                     }
         
                   }
                 }
         
               }
             }
         ```
         
      5. 发布镜像到阿里云仓库
   
         - 创建登录阿里云仓库凭证
   
         - 修改`Jenkinsfile`文件环境变量
   
           ```
             environment {
               DOCKER_CREDENTIAL_ID = 'dockerhub-id'
               GITHUB_CREDENTIAL_ID = 'github-id'
               KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
               REGISTRY = 'registry.cn-guangzhou.aliyuncs.com'
               DOCKERHUB_NAMESPACE = 'dx-yst'
               GITHUB_ACCOUNT = 'kubesphere'
               APP_NAME = 'devops-java-sample'
             }
           ```
   
         - 添加dokcer登录发布命令
   
           > 注意:下面的`$DOCKER_PWD_VAR`和`$DOCKER_USER_VAR`要在流水线里,添加步骤里->添加凭证
   
           ```sh
           withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
           
               #登录阿里云仓库
               sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
           
               #dokcer tage hospital-manage:latest registry.cn-guangzhou.aliyuncs.com/dx-yst/hospital-manage:SNAPSHOT-22
               sh 'docker tag hospital-manage:latest $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
           
               #docker push registry.cn-guangzhou.aliyuncs.com/dx-yst/hospital-manage:SNAPSHOT-22
               sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
           
           }
           ```
           
         - 批量发布到阿里云仓库
         
           ```
               stage('default-3') {
                 parallel {
                   stage('发布hospital-manage项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag hospital-manage:latest $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布server-gateway项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag server-gateway:latest $REGISTRY/$DOCKERHUB_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('推送service-cmn镜像') {
                       agent none
                       steps {
                           container('maven') {
                               withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                                   sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                                   sh 'docker tag service-cmn:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
                                   sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
                               }
           
                           }
           
                       }
                   }
                   stage('发布service-hosp项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-hosp:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布service-order项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-order:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布service-oss项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-oss:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
                   stage('发布service-sms项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-sms:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布service-statistics项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-statistics:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布service-task项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-task:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                   stage('发布service-user项目') {
                     agent none
                     steps {
                       container('maven') {
                         withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                           sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                           sh 'docker tag service-user:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
                           sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
                         }
           
                       }
           
                     }
                   }
           
                 }
               }
           ```
         
      6. 部署到开发环境或者部署到生产环境
   
         1. 添加审核，可以要求@某人通过是否进行部署
   
         2. 使用配置文件`deploy.yaml`部署各个微服务，每个微服务下都有个`deploy`目录文件夹配置文件进行部署
   
            > 在`kubernetesDeploy`下进行配置，要执行`deploy.yaml`文件 ,各节点必须要有权限执行，默认节点都是没有权限的，需要添加权限
            >
            > ```sh
            > kubectl apply -f deploy.yaml
            > ```
            >
            > 默认所有kubectl的权限文件在当前`~`目录下的`/root/.kube`的`config`文件 ,配置凭证
            >
            > ```
            > 凭证ID: demo-kubeconfig (默认的环境变量，流水线配置里已经默认有了KUBECONFIG_CREDENTIAL_ID)
            > 类型 : kubeconfig
            > ```
   
         3. 配置`deploy.yaml`文件目录
   
            ```
            hospital-manage/deploy/**
            ```
   
         4. 因为部署是需要到私有仓库里进行下载的，所以也需要部署私有仓库的配置（之前配置的是上传私有仓库的配置），可以看下面的`deploy.yaml`文件`aliyun-docker-hub`,变量也需要在`Jenkinsfile`配置环境变量
   
            > **注意：在项目下(不是流水线项目)的->配置中心->密钥进行配置阿里云仓库**
            >
            > `imagePullSecrets.name`
            >
            > **下面的参考`deploy.yaml`配置文件里的`namespace`对应**
            
         5. 批量部署
   
            ```
            stage('default-4') {
                  parallel {
                    stage('hospital-manage - 部署到dev环境') {
                      agent none
                      steps {
                        kubernetesDeploy(configs: 'hospital-manage/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                      }
                    }
            
                    stage('server-gateway - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'server-gateway/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
            
                    stage('service-cmn - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-cmn/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
            
                    stage('service-hosp - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-hosp/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
            
                    stage('service-order - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-order/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
            
            
                            stage('service-oss - 部署到dev环境') {
                                agent none
                                steps {
                                    kubernetesDeploy(configs: 'service/service-oss/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                                }
                            }
                    stage('service-sms - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-sms/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
                    stage('service-statistics - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-statistics/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
                    stage('service-task - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-task/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
                    stage('service-user - 部署到dev环境') {
                        agent none
                        steps {
                            kubernetesDeploy(configs: 'service-user/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                        }
                    }
            ```
   
       7. 添加邮件发送
   
          ```
                  //1、配置全系统的邮件： 平台配置->通知管理->邮件  全系统的监控
                  //2、修改ks-jenkins的配置，里面的邮件；   流水线发邮件
                  stage('发送确认邮件') {
                      agent none
                      steps {
                          mail(to: '17512080612@163.com', subject: '构建结果', body: "构建成功了  $BUILD_NUMBER")
                      }
                  }
          ```
   
   4. 前端项目
   
      1. `yygh-admin`
      - `npm run build` 会生成`dist`目录，放到nginx的html下，即可运行
      2. `yygh-site`
         - `npm install --registry=https://registry.npm.taobao.org ` 安装项目依赖
         - `npm run build` 对项目打包，
         - 打包完成后把 `.nuxt ,static, nuxt.config.js, package.json ` 这四个关键文件复制到 `node `环境。先`npm install`再使用`npm run start` 即可运行
   
   5. webhook
   
      - 每个项目，都有流水线文件
      
      - 每次修改完项目，手动点击运行
      
      - 希望，每次修改完项目，代码推送，流水线能自动运行
      
        - 写代码并提交------> gitee ---------> 给指定的地方发请求（webhook）------> kubesphere平台感知到 -----> 自动启动流水线继续运行
	     - http://139.198.165.238:30880/devops_webhook/git/?url=https://gitee.com/lee_friend/yst-parent-master
      
        > 必须在初始化流水线时就定义好要下载的项目路径初始化,才能使用只方式,自定义流水线是不会生成`webhook`
   
   
   4. 生产与开发配置隔离
   
   6. 完整后端流水线`Jenkinsfile`文件参考
   
      ```
      pipeline {
        agent {
          node {
            label 'maven'
          }
      
        }
        stages {
          stage('拉取代码') {
            agent none
            steps {
              container('maven') {
                git(url: 'https://gitee.com/lee_friend/yst-parent-master.git', credentialsId: 'gitee-id', branch: 'master', changelog: true, poll: false)
                sh 'ls -al'
              }
      
            }
          }
      
          stage('项目编译') {
            agent none
            steps {
              container('maven') {
                sh 'ls -al'
                sh 'mvn clean package -Dmaven.test.skip=true'
                sh 'ls hospital-manage/target'
              }
      
            }
          }
      
          stage('default-2') {
            parallel {
              stage('构建hospital-manage镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./hospital-manage/target'
                    sh 'docker build -t hospital-manage:latest -f hospital-manage/Dockerfile  ./hospital-manage/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建server-gateway镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./server-gateway/target'
                    sh 'docker build -t server-gateway:latest -f server-gateway/Dockerfile  ./server-gateway/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-cmn镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-cmn/target'
                    sh 'docker build -t service-cmn:latest -f service/service-cmn/Dockerfile  ./service/service-cmn/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-hosp镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-hosp/target'
                    sh 'docker build -t service-hosp:latest -f service/service-hosp/Dockerfile  ./service/service-hosp/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-order镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-order/target'
                    sh 'docker build -t service-order:latest -f service/service-order/Dockerfile  ./service/service-order/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-oss镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-oss/target'
                    sh 'docker build -t service-oss:latest -f service/service-oss/Dockerfile  ./service/service-oss/'
                    sh 'docker images'
                  }
      
                }
              }
              stage('构建service-sms镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-sms/target'
                    sh 'docker build -t service-sms:latest -f service/service-sms/Dockerfile  ./service/service-sms/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-statistics镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-statistics/target'
                    sh 'docker build -t service-statistics:latest -f service/service-statistics/Dockerfile  ./service/service-statistics/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-task镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-task/target'
                    sh 'docker build -t service-task:latest -f service/service-task/Dockerfile  ./service/service-task/'
                    sh 'docker images'
                  }
      
                }
              }
      
              stage('构建service-user镜像') {
                agent none
                steps {
                  container('maven') {
                    sh 'ls -al  ./service/service-user/target'
                    sh 'docker build -t service-user:latest -f service/service-user/Dockerfile  ./service/service-user/'
                    sh 'docker images'
                  }
      
                }
              }
      
            }
          }
      
          stage('default-3') {
            parallel {
              stage('发布hospital-manage项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag hospital-manage:latest $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布server-gateway项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag server-gateway:latest $REGISTRY/$DOCKERHUB_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('推送service-cmn镜像') {
                  agent none
                  steps {
                      container('maven') {
                          withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                              sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                              sh 'docker tag service-cmn:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
                              sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
                          }
      
                      }
      
                  }
              }
              stage('发布service-hosp项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-hosp:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布service-order项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-order:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布service-oss项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-oss:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
              stage('发布service-sms项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-sms:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布service-statistics项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-statistics:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布service-task项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-task:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
              stage('发布service-user项目') {
                agent none
                steps {
                  container('maven') {
                    withCredentials([usernamePassword(credentialsId : 'aliyun-docker-registry' ,usernameVariable : 'DOCKER_USER_VAR' ,passwordVariable : 'DOCKER_PWD_VAR' ,)]) {
                      sh 'echo "$DOCKER_PWD_VAR" | docker login $REGISTRY -u "$DOCKER_USER_VAR" --password-stdin'
                      sh 'docker tag service-user:latest $REGISTRY/$DOCKERHUB_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
                      sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
                    }
      
                  }
      
                }
              }
      
            }
          }
      
          stage('default-4') {
            parallel {
              stage('hospital-manage - 部署到dev环境') {
                agent none
                steps {
                  kubernetesDeploy(configs: 'hospital-manage/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                }
              }
      
              stage('server-gateway - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'server-gateway/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
      
              stage('service-cmn - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-cmn/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
      
              stage('service-hosp - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-hosp/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
      
              stage('service-order - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-order/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
      
      
                      stage('service-oss - 部署到dev环境') {
                          agent none
                          steps {
                              kubernetesDeploy(configs: 'service/service-oss/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                          }
                      }
              stage('service-sms - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-sms/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
              stage('service-statistics - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-statistics/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
              stage('service-task - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-task/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
              stage('service-user - 部署到dev环境') {
                  agent none
                  steps {
                      kubernetesDeploy(configs: 'service-user/deploy/**', enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
                  }
              }
      
      	 //1、配置全系统的邮件： 平台配置->通知管理->邮件  全系统的监控
              //2、修改ks-jenkins的配置，里面的邮件；   流水线发邮件
              stage('发送确认邮件') {
                  agent none
                  steps {
                      mail(to: '17512080612@163.com', subject: '构建结果', body: "构建成功了  $BUILD_NUMBER")
                  }
              }
            }
          }
      
        }
        environment {
          DOCKER_CREDENTIAL_ID = 'dockerhub-id'
          GITHUB_CREDENTIAL_ID = 'github-id'
          KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
          REGISTRY = 'registry.cn-guangzhou.aliyuncs.com'
          DOCKERHUB_NAMESPACE = 'dx-yst'
          GITHUB_ACCOUNT = 'kubesphere'
          APP_NAME = 'devops-java-sample'
          ALIYUNHUB_NAMESPACE = 'dx-yst'
        }
        parameters {
          string(name: 'TAG_NAME', defaultValue: '', description: '')
        }
      }
      ```
   
   5. `deploy.yaml`
   
      ```yaml
      apiVersion: apps/v1
      kind: Deployment
      metadata:
        labels:
          app: hospital-manage
        name: hospital-manage
        namespace: his   #一定要写名称空间
      spec:
        progressDeadlineSeconds: 600
        replicas: 1
        selector:
          matchLabels:
            app: hospital-manage
        strategy:
          rollingUpdate:
            maxSurge: 50%
            maxUnavailable: 50%
          type: RollingUpdate
        template:
          metadata:
            labels:
              app: hospital-manage
          spec:
            imagePullSecrets:
              - name: aliyun-docker-hub  #提前在项目下配置访问阿里云的账号密码
            containers:
              - image: $REGISTRY/$ALIYUNHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
       #下面因为Spring Boot 版本比较低,造成部署容器后一直无法启动成功,先把健康检查屏蔽
       #         readinessProbe:
       #           httpGet:
       #             path: /actuator/health
       #             port: 8080
       #           timeoutSeconds: 10
       #           failureThreshold: 30
       #           periodSeconds: 5
                imagePullPolicy: Always
                name: app
                ports:
                  - containerPort: 8080
                    protocol: TCP
                resources:
                  limits:
                    cpu: 300m
                    memory: 600Mi
                terminationMessagePath: /dev/termination-log
                terminationMessagePolicy: File
            dnsPolicy: ClusterFirst
            restartPolicy: Always
            terminationGracePeriodSeconds: 30
      ---
      apiVersion: v1
      kind: Service
      metadata:
        labels:
          app: hospital-manage
        name: hospital-manage
        namespace: his
      spec:
        ports:
          - name: http
            port: 8080
            protocol: TCP
            targetPort: 8080
        selector:
          app: hospital-manage
        sessionAffinity: None
        type: ClusterIP
      ```
      
      
   
   
