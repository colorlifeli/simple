--保存从网站获得的原始的未经转换的数据。暂时不使用此表
create table sto_realtime_original (
code varchar(10),
str1 varchar(15),str2 varchar(15),str3 varchar(15),str4 varchar(15),str5 varchar(15),
str6 varchar(15),str7 varchar(15),str8 varchar(15),str9 varchar(15),str10 varchar(15),
str11 varchar(15),str12 varchar(15),str13 varchar(15),str14 varchar(15),str15 varchar(15),
str16 varchar(15),str17 varchar(15),str18 varchar(15),str19 varchar(15),str20 varchar(15),
str21 varchar(15),str22 varchar(15),str23 varchar(15),str24 varchar(15),str25 varchar(15),
str26 varchar(15),str27 varchar(15),str28 varchar(15),str29 varchar(15),str30 varchar(15),
str31 varchar(15),str32 varchar(15),str33 varchar(15),str34 varchar(15),str35 varchar(15),
time_ varchar(10),
source varchar(10),
flag varchar(2)
);

create table sto_realtime (
code varchar(10),
yClose varchar(15),tOpen varchar(15),now varchar(15),high varchar(15),low varchar(15),deals varchar(20),
dealsum varchar(15),
time_ varchar(10),
source varchar(10),
flag varchar(2)
);

create table sto_day (
code varchar(10),
date_ date,open_ varchar(15),high varchar(15),low varchar(15),close_ varchar(15),volume varchar(20),factor varchar(15),
source varchar(10),
PRIMARY KEY(code,date_)
);

create table sto_day_tmp (
code varchar(10),
date_ date,open_ varchar(15),high varchar(15),low varchar(15),close_ varchar(15),volume varchar(20),factor varchar(15),
source varchar(10),
PRIMARY KEY(code,date_)
);

create table sto_code (
code varchar(10),
name varchar(50),
market varchar(10),
source varchar(10),
valid varchar(1),
flag varchar(2),  --01:stop 99：获取错误
type_ varchar(2), --1：指数
code_sina varchar(15),
code_yahoo varchar(15),
PRIMARY KEY(code,market)
);

create table dd_code (
code varchar(20),
desc_cn varchar(30),
source varchar(10),
valid varchar(1),
flag varchar(2)
);

create table dd_value (
code varchar(20),
codeValue varchar(20),
value_desc_cn varchar(30),
valid varchar(1)
);

create table sto_day_mid (
code varchar(10),
date_ date,open_ varchar(15),high varchar(15),low varchar(15),close_ varchar(15),volume varchar(20),
source varchar(10),
flag varchar(2),
step int
);

create table sto_operation (
sn int,
code varchar(10),
oper varchar(2), --操作，买(1) or 卖(2), 0表示不操作
num int, --操作数量
price decimal, --单价
sum decimal, --操作总价
total int, --当前拥有数量
remain decimal, --余额
flag varchar(2),
date_ date
);

create table sto_oper_sum (
code varchar(10),
name varchar(50),
buys int, --买入次数
sells int, --卖出次数
times int, --total 为0的次数（即卖光了）
winTimes int, --赢利次数
loseTimes int, --亏损次数
lastRemain decimal, --最后一次卖光时的余额
minRemain decimal, --最小余额,即最大投资
flag varchar(2)  --最后一次卖过时情况，01 表示数据过好还过坏，即认为是异常数据
);

--20180525 历史数据下载进度表，记录下载到了哪一天。主要是因为不少股会停牌，
--不能确定下载到哪一天。
--- ****  第一次要先初始化：insert into his_data_progress(code) select code from sto_code;
create table his_data_progress (
code varchar(10),
lastDate date
);

--20151022
--alter table sto_code add code_sina varchar(15);

--20151028
--alter table sto_code add code_yahoo varchar(15);

update sto_code t set code_sina=(select market||code from sto_code where code=t.code and market=t.market);

--20151106
create index on sto_day_tmp (code);


truncate table sto_realtime_original;
truncate table sto_realtime;
truncate table sto_day;
truncate table sto_code;
truncate table dd_code;
truncate table dd_value;
truncate table sto_operation;
truncate table sto_oper_sum;
drop table sto_realtime_original;
drop table sto_realtime;
drop table sto_day;
drop table sto_code;
drop table dd_code;
drop table dd_value;
drop table sto_operation;
drop table sto_oper_sum;