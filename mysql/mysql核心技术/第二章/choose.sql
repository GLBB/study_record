create table my_table(
	today datetime,
	name char(20)
);

show create table my_table;

insert into my_table values(now(), 'a');
insert into my_table values(now(), 'a');
insert into my_table values(now(), null);
insert into my_table values(now(), '');