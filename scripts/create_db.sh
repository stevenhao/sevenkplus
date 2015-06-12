read -p "Username: " uname
read -sp "Password: " pass

mysql --user="$uname" --password="$pass" -Bse "
drop database if exists sevenkplus;
create database sevenkplus;
use sevenkplus;
create table player (id int primary key auto_increment, name varchar(255) unique not null);
insert into player (name) values ('sevenk');
insert into player (name) values ('stevenk');"
echo 'Success!'
