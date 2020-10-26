# simple

2016-10-10
增加了sina复权数据的来源。
为不影响以前的数据，增加新的表：
create table sto_day_tmp2 (
code varchar(10),
date_ date,open_ varchar(15),high varchar(15),low varchar(15),close_ varchar(15),volume varchar(20),factor varchar(15),
source varchar(10),
PRIMARY KEY(code,date_)
);

create index on sto_day_tmp2 (code);


2016-10-21
当前测试方案，当 pos<-2和pos=-1，和非中枢时，pos=-1 都买时，结果最理想。卖时机一般取1.2即可，起点是2013－01－01时，卖时机取越大结果越好，符合大盘上升时，结果更好的预期。
只在 非中枢，pos=-1时，结果不错，且买卖次数比较少，当前倾向于按此方案来操作

2016-11-22
action 改成是非单例的。每次调用 new 一个实例。
而 service 仍然是单例的，所以这个action 所有实例都共享的数据，应放在 service 里。

2018-06-07
增加历史数据下载进度表；初始化：
insert into his_data_progress(code)
select code from STO_CODE ;

2019-12-19
测试新的算法思想：
1.中枢改用最高点和最低点。
2.当第2个中枢与前一中枢有交叉时。之前是舍弃这个中枢。但也表示前一中枢比较稳定。如果中枢稳定的话，在底分型时买，顶分型时卖。
代码改动：
	新增CentralInfo2。
	Simulator 新增函数handle3
	新增Compute3类

2020-10-26
- 简化整合了一下代码：新建了 simulator3类,希望更专注于变化点。尝试了一些策略，但效果不好。
- 对compute3进行了一点优化。增加了一个控制函数：check。可以控制连续买几次就不再买。本想实现控制总的投入金额，但因为是先执行单个股票所有计算再到下一个股票，因此没办法控制总投入，因为在某一天之前，不知道后面的股票会买多少。
- 发现h2数据库不同版本不兼容，用新版本尝试连接数据库失败之后，再用原来版本也连接不了了...  全新初始化数据库步骤：1.执行 init.sql 2.到交易所下载所有股票代码（todo上有链接），整合成 codes.csv文件，执行 InitCodes 类。 3.初始化 his_data_progress  4.执行 GetStockHisData 类



# 系统结构
## common 基础功能，以及公共函数与设置  
## service 对应web，页面后端功能  
## web 页面action  
## net stock相关  
### supplier 数据来源。主要来源于新浪与雅虎网页数据。当前雅虎数据已不能用。类型有2种
* 历史数据。每日数据。包括开盘、收盘、最高、最低
* 每日实时数据
当前没有对实时数据进行分析
### dayHandler 对数据进行分析与处理。当前采用的是缠论的概念：顶、底分型与中枢。忽略了线段的概念。
中枢的处理放在了model类里：CentralInfo
### simulator 将 dayHandler与centralinfo结合在一起。中枢涉及到较长时间段内的数据，由 simulator分析得出。并在此判断是否可买可卖。
### compute 如何对所有股票进行处理。包括买卖策略。并计算盈亏，用历史数据在测试分析是否有效，是否能用于实际。








