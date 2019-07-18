　Git基本常用命令如下：

　　**mkdir**：         XX (创建一个空目录 XX指目录名)

　　**pwd**：          显示当前目录的路径。

　　**git init**          把当前的目录变成可以管理的git仓库，生成隐藏.git文件。

　　**git add XX**       把xx文件添加到暂存区去。

　　**git commit –m “XX”**  提交文件 –m 后面的是注释。

　　**git status**        查看仓库状态

　　**git diff  XX**      查看XX文件修改了那些内容

　　**git log**          查看历史记录

　	**git reset  --hard HEAD^** 或者 **git reset  --hard HEAD~** 回退到上一个版本

　　(如果想回退到100个版本，使用git reset –hard HEAD~100 )

　　**cat XX**         查看XX文件内容

　　**git reflog**       查看历史记录的版本号id

　　**git checkout -- XX**  把XX文件在工作区的修改全部撤销。

　　**git rm XX**          删除XX文件

　　**git remote add origin https://github.com/tugenhua0707/testgit** 关联一个远程库

　　**git push –u(第一次要用-u 以后不需要) origin master** 把当前master分支推送到远程库

　　**git clone https://github.com/tugenhua0707/testgit**  从远程库中克隆

　　**git checkout –b dev**  创建dev分支 并切换到dev分支上

　　**git branch**  查看当前所有的分支

　　**git checkout master** 切换回master分支

　　**git merge dev**    在当前的分支上合并dev分支

　　**git branch –d dev** 删除dev分支

　　**git branch name**  创建分支

　　**git stash** 把当前的工作隐藏起来 等以后恢复现场后继续工作

　　**git stash list** 查看所有被隐藏的文件列表

　　**git stash apply** 恢复被隐藏的文件，但是内容不删除

　　**git stash drop** 删除文件

　　**git stash pop** 恢复文件的同时 也删除文件

　　**git remote** 查看远程库的信息

　　**git remote –v** 查看远程库的详细信息

　　**git push origin master**  Git会把master分支推送到远程库对应的远程分支上


# GIT #

**基本操作**

	设置签名
	项目级别/仓库级别：仅在当前本地库范围内有效
	git config user.name xxxxx
	git config user.email xxxxx@xxxx
	信息保存位置：当前项目目录下的./.git/config文件
	
	系统用户级别:登录当前操作系统的用户范围
	git config --global user.name xxxx
	git config --global user.email xxxxx@xxxx
	信息保存位置：~/.gitconfig文件
	
	注意：一般在实际过程中只要设置系统级别就可以了

	状态查看
	git status 查看工作区、暂存区状态

	添加
	git add [file name]	将工作区的“新建/修改”添加到暂存区

	提交
	git commit -m "commit message" [file name] 将暂存区的内容提交到本地库
	
	查看历史记录
	git log 最完整的方式显示，多屏显示控制方式: 空格向下翻面 b向上翻页 q退出
	git log --pretty=oneline	以简洁方式显示
	git log --oneline 更简洁方式显示(缩短了HASH值)
	git reflog HEAD@{移动到当前版本需要多少步} 有HEAD的版本

	前进后退版本
	基于索引值操作[推荐]
	git reset --hard [局部索引值]
	使用^符号:只能后退
	git reset --hard HEAD^
	注：一个^表示后退一步，n个表示后退n步
	使用~符号:只能后退
	git reset --hard HEAD~n
	注:表示后退n步

	reset命令的三个参数对比
		--soft参数  仅仅在本地库移动HEAD指针
		--mixed参数 在本地库移动HEAD指针
					重置暂存区
		--hard参数  在本地库移动HEAD指针
					重置暂存区
					重置工作区
			
	工作区 		暂存区 		本地库
	本地目录		add后		commit后
	注:未提交到暂存区（红），未提交到本地库（绿）				

	删除文件并找回
	注意:删除前，文件存在时的状态必须提交到了本地库，否则怎样都找不回来
	操作:git reset --hard [指针位置]
		删除操作已提交到本地库：指针位置指向历史记录
		删除操作尚未提交到本地库：指针位置使用HEAD

	比较文件差异
	git diff [文件名] 将工作区中的文件和暂存区进行比较
	git diff [本地库中历史版本][文件名] 将工作区中的文件和本地库历史记录比较
	不带文件名比较多个文件

**分支管理**

	创建分支 git branch [分支名]
	查看分支 git branch -v
	切换分支 git checkout [分支名]
	合并分支 
		第一步:切换到接受修改的分支（被合并，增加新内容）上git checkout [被合并分支名]
		第二步：执行merge命令git merge [新内容分支名]
	解决冲突
		1，编辑文件，删除特殊符号
		2，把文件修改到满意的程度，保存退出
		3，git add [文件名]
		4，git commit -m "日志信息"
			注意：此时commit一定不能具体文件名

**GitHub**
	
	创建远程库地址别名
	git remote -v	查看当前所有远程地址别名
	git remote add [别名][远程地址]

	推送 git push origin master

	克隆 gitclone [远程地址]
		效果：完整的把远程库下载到本地，创建origin远程地址别名，初始化本地库

	拉取 pull=fetch+merge
	git fetch [远程库地址别名][远程分支名]		//先抓取文件，不会合并文件
	git merge [远程库地址别名/远程分支名]		//合并文件
	git pull [远程库地址别名][远程分支名]		//fetch+merge抓取合并

	推送产生的冲突（本地与远程库的冲突）
	要点：如果不是基于GitHub远程库的最新版所做的修改,不能推送,必须先拉取
		 拉取下来后如果进入冲突状态,则按照"分支冲突解决"操作解决即可。

	SSH登录
	1进入当前用户的家目录 cd ~
	2删除.ssh目录 rm -rvf .ssh
	3运行命令生成.ssh密钥目录 ssh-keygen -t rsa -C xxxxxx@xxx.com