# Docker #

**帮助命令**

	docker version
	docker info
	docker --help

**镜像命令**
	
	docker images [OPTIONS]		//列出本地主机上的镜像
		OPTIONS说明：		-a :列出本地所有的镜像（含中间映像层）
							-q :只显示镜像ID。
							--digests :显示镜像的摘要信息
							--no-trunc :显示完整的镜像信息

	docker search [OPTIONS] 镜像名字
		OPTIONS说明：		--no-trunc : 显示完整的镜像描述
							-s : 列出收藏数不小于指定值的镜像。
							--automated : 只列出 automated build类型的镜像；

	docker pull 镜像名字[:TAG]

	docker rmi 某个XXX镜像名字ID	//删除镜像
			删除单个	docker rmi  -f 镜像ID
			删除多个	docker rmi -f 镜像名1:TAG 镜像名2:TAG 
			删除全部	docker rmi -f $(docker images -qa)

**容器命令**

	docker run [OPTIONS] IMAGE [COMMAND] [ARG...]	//新建并启动容器
		 OPTIONS说明（常用）：有些是一个减号，有些是两个减号
				--name="容器新名字": 为容器指定一个名称；
				-d: 后台运行容器，并返回容器ID，也即启动守护式容器；
				-i：以交互模式运行容器，通常与 -t 同时使用；
				-t：为容器重新分配一个伪输入终端，通常与 -i 同时使用；
				-P: 随机端口映射；
				-p: 指定端口映射，有以下四种格式
				      ip:hostPort:containerPort
				      ip::containerPort
				      hostPort:containerPort
				      containerPort

	docker ps [OPTIONS]		//列出当前所有正在运行的容器
		OPTIONS说明（常用）：
			-a :列出当前所有正在运行的容器+历史上运行过的
			-l :显示最近创建的容器。
			-n：显示最近n个创建的容器。
			-q :静默模式，只显示容器编号。
			--no-trunc :不截断输出。

	exit	//容器停止退出
	ctrl+P+Q	//容器不停止退出
	docker start 容器ID或者容器名		//启动容器
	docker restart 容器ID或者容器名	//重启容器
	docker stop 容器ID或者容器名		//停止容器
	docker kill 容器ID或者容器名		//强制停止容器
	docker rm 容器ID					//删除已停止的容器
		一次性删除多个容器	docker rm -f $(docker ps -a -q)
							docker ps -a -q | xargs docker rm

	重要
	docker logs -f -t --tail 容器ID		//查看容器日志
			*   -t 是加入时间戳
			*   -f 跟随最新的日志打印
			*   --tail 数字 显示最后多少条
	docker top 容器ID					//查看容器内运行的进程
	docker inspect 容器ID				//查看容器内部细节
	
	/**进入正在运行的容器并以命令行交互**/
	docker exec -it 容器ID bashShell
	docker attach 容器ID
		/**
		上述两个区别
			attach 直接进入容器启动命令的终端，不会启动新的进程
			exec 是在容器中打开新的终端，并且可以启动新的进程
		**/

	docker cp  容器ID:容器内路径 目的主机路径		//从容器内拷贝文件到主机上


