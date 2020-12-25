# ComicTools #
将BiliBili漫画缓存文件转换成可直接观看的webp文件

## 新版本说明 ##

实测3.8.0版本，BiliBili漫画缓存目录已迁移至"/data/data/com.bilibili.comic/files/down"，改目录为APP私有目录，无法直接访问文件夹。且图片文件已不再加密（直接更改文件格式即可），因此此项目目前可视为一个BiliBili漫画文件名查询更改工具。

3.8.0版本后使用说明，**使用有root权限的手机或模拟器**，将BiliBili漫画缓存目录（/data/data/com.bilibili.comic/files/down）下文件复制到下载目录（/storage/emulated/0/Download），再使用APP进行转换


*1. 必须复制到Download目录下，否则无法网络查询漫画名*

*2. 3.8.0为实测版本，具体在哪个版本修改了缓存路径未知*

## 下载 ##
[前往安装包下载页面](https://github.com/xyoye/ComicTools/tree/master/app/release)

## 使用 ##
选择源目录，存在BiliBili漫画缓存目录将自动打开，选中需要转换的目录

APP将联网自动识别漫画名及其它信息

输出路径默认为BiliBili漫画缓存目录，待信息确认完成，可点击“开始转换”

## 实现 ##
漫画名通过漫画ID即文件夹名，联网获取

章节名在联网获取到的信息中

页顺序根据章节目录下“index.dat”，解密获取

## 录屏 ##
<div>
	<img src="https://github.com/xyoye/ComicTools/blob/master/ScreenRecorder/2020-1-17.gif" width="400px">
</div>
