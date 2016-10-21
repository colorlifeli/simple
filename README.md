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