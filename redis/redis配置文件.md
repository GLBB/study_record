# redis.conf 配置文件

题外话：

看到一篇比较深度解析 redis 的文章，做个记录：

https://www.cnblogs.com/kismetv/p/8654978.html

**启动**

启动 redis 时，可以指定配置文件::

```
./redis-server /path/to/redis.conf
```

**内存占用**

在 redis.conf 中可以配置 redis 内存使用量:

```
# Note on units: when memory size is needed, it is possible to specify
# it in the usual form of 1k 5GB 4M and so forth:
#
# 1k => 1000 bytes
# 1kb => 1024 bytes
# 1m => 1000000 bytes
# 1mb => 1024*1024 bytes
# 1g => 1000000000 bytes
# 1gb => 1024*1024*1024 bytes
#
# units are case insensitive so 1GB 1Gb 1gB are all the same.
4GB
```

**导入其他配置文件**

在 redis.conf 中导入其他配置文件

```
include /path/to/local.conf
```

**模块**

```
# Load modules at startup. If the server is not able to load modules
# it will abort. It is possible to use multiple loadmodule directives.
#
# loadmodule /path/to/my_module.so
# loadmodule /path/to/other_module.so
```

## 网络方面配置

**允许哪些地址访问**

```
bind 192.168.1.100 10.0.0.1
bind 127.0.0.1 ::1
```

默认只允许本地回环访问

**保护模式**

避免被互联网上的任意主机访问，默认保护模式是打开的。

如果保护模式打开，且没有使用 bind 指定哪些主机可以访问，也没有配置密码，那么redis 只允许本地循环访问。

**端口**

```
port 6379
```

**tcp 请求队列**

如果 redis 面临大量请求，可以将 tcp 请求队列的值设置得大一些，同时 tcp 请求队列在 /proc/sys/net/core/somaxconn 系统文件中也有限制，所以想要达到效果，要同时提高系统配置中得 tcp 请求队列和 redis tcp-backlog。

```
tcp-backlog 511
```

配置/proc/sys/net/core/somaxconn 中的 /proc/sys/net/core/somaxconn 的 tcp_max_syn_backlog 属性。

**Unix Socket**

```
# Unix socket.
#
# Specify the path for the Unix socket that will be used to listen for
# incoming connections. There is no default, so Redis will not listen
# on a unix socket when not specified.
#
# unixsocket /tmp/redis.sock
# unixsocketperm 700
```

**当客户端空闲多少秒时关闭连接**

```
timeout 0
```

指定为 0 表示不关闭

**TCP_KeepAlive**

当和客户端连接空闲时，多久发送一个 ACK 报文.

```
tcp-keepalive 300
```

默认 300 秒

## 一般配置

**后台运行**

```
daemonize no
```

默认不后台运行

**superversion**

```
# If you run Redis from upstart or systemd, Redis can interact with your
# supervision tree. Options:
#   supervised no      - no supervision interaction
#   supervised upstart - signal upstart by putting Redis into SIGSTOP mode
#   supervised systemd - signal systemd by writing READY=1 to $NOTIFY_SOCKET
#   supervised auto    - detect upstart or systemd method based on
#                        UPSTART_JOB or NOTIFY_SOCKET environment variables
# Note: these supervision methods only signal "process is ready."
#       They do not enable continuous liveness pings back to your supervisor.
supervised no
```

**pid 文件**

当 redis 后台运行时，会创建 /var/run/redis.pid 这样一个 pid 文件

```
pidfile /var/run/redis_6379.pid
```

**verbosity level**

猜测和日志记录级别差不多

```
# Specify the server verbosity level.
# This can be one of:
# debug (a lot of information, useful for development/testing)
# verbose (many rarely useful info, but not a mess like the debug level)
# notice (moderately verbose, what you want in production probably)
# warning (only very important / critical messages are logged)
loglevel notice
```

**log 文件**

```
logfile ""
```

没有指定，会使用标准输出，如果后台运行，且没有指定 logfile 的话， 日志消息会发送到 /dev/null。

/dev/null 会丢弃任何消息

**没看懂**

```
# To enable logging to the system logger, just set 'syslog-enabled' to yes,
# and optionally update the other syslog parameters to suit your needs.
# syslog-enabled no

# Specify the syslog identity.
# syslog-ident redis

# Specify the syslog facility. Must be USER or between LOCAL0-LOCAL7.
# syslog-facility local0
```

**数据库**

设置数据库的数量

```
databases 16
```

默认 16 个数据库，客户端可以使用 select 命令来指定使用哪个数据库

**logo**

启动时显示 logo, 默认 yes

```
always-show-logo yes
```

## 快照

**save**

保存数据库中的数据到磁盘中。

```
save 900 1 // 至少 1 个 key 改变了，900 秒后会保存磁盘
save 300 10
save 60 10000
```

**stop-writes-on-bgsave-error**

```
# By default Redis will stop accepting writes if RDB snapshots are enabled
# (at least one save point) and the latest background save failed.
# This will make the user aware (in a hard way) that data is not persisting
# on disk properly, otherwise chances are that no one will notice and some
# disaster will happen.
#
# If the background saving process will start working again Redis will
# automatically allow writes again.
#
# However if you have setup your proper monitoring of the Redis server
# and persistence, you may want to disable this feature so that Redis will
# continue to work as usual even if there are problems with disk,
# permissions, and so forth.
stop-writes-on-bgsave-error yes
```

**rdbcompression**

是否使用 LZF 算法在 .rdb 文件压缩字符串。

关闭可以减少 CPU 占用时间，但是会导致数据集更大。

默认开启

```
rdbcompression yes
```

**rdbchecksum**

```
# Since version 5 of RDB a CRC64 checksum is placed at the end of the file.
# This makes the format more resistant to corruption but there is a performance
# hit to pay (around 10%) when saving and loading RDB files, so you can disable it
# for maximum performances.
#
# RDB files created with checksum disabled have a checksum of zero that will
# tell the loading code to skip the check.
rdbchecksum yes
```

提升性能的一个选项。

**dump file name**

```
# The filename where to dump the DB
dbfilename dump.rdb
```

dum.rdb 默认会放在启动目录下, 可以指定 dir ，指定 rdb 文件放在哪

```
dir ./
```

## 复制

以后再看