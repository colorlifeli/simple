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
