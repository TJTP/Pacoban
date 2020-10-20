# HSEA课程作业一: Pacoban游戏
## 文件概述
* 采用[GVG-AI](http://www.gvgai.net/)游戏框架
* `src.tracks.multiPlayer`中有各种控制器
    * 自行设计的启发式搜索方法在`src.tracks.multiPlayer.myController`中
## 任务
* 自行设计启发式搜索方法, 使得最后两个玩家能获得的总分最高 

## 分支说明
* `master`分支实现的算法将吃道具作为主要目标, 得分较低
* `advanced`分支实现的算法将击杀幽灵作为主要目标, 得分较高