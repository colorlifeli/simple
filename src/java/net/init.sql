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
yClose varchar(15),tOpen varchar(15),now varchar(15),high varchar(15),low varchar(15),deals varchar(15),
dealsum varchar(15),
time_ varchar(10),
source varchar(10),
flag varchar(2)
);

create table sto_day (
code varchar(10),
date_ date,open_ varchar(15),high varchar(15),low varchar(15),close_ varchar(15),volume varchar(15),
source varchar(10)
);

create table sto_code (
code varchar(10) primary key,
name varchar(50),
market varchar(10),
source varchar(10),
valid varchar(1),
flag varchar(2)
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

truncate table sto_realtime_original;
truncate table sto_realtime;
truncate table sto_day;
truncate table sto_code;
truncate table dd_code;
truncate table dd_value;
drop table sto_realtime_original;
drop table sto_realtime;
drop table sto_day;
drop table sto_code;
drop table dd_code;
drop table dd_value;