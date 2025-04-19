# LiteSQL

LiteSQL 是一个基于 Java 语言实现的简易 MySQL 数据库，旨在提供核心数据库功能的实现，包括数据存储、事务管理、并发控制、索引构建等。LiteSQL 模拟了日志管理和事务状态查询等高级特性，适用于学习数据库原理及相关技术的开发者。

## 特性

- **数据存储**：实现基本的数据存储和访问。
- **事务管理**：支持 ACID 事务，具备提交、回滚和事务隔离功能。
- **多版本并发控制 (MVCC)**：实现并发控制，支持 RC 和 RR 事务隔离级别。
- **索引系统**：基于 B+ 树构建索引，支持高效的范围查找和定位。
- **日志管理**：模拟日志管理功能，用于事务恢复和回滚。
- **死锁检测**：具备简单的死锁检测和处理机制。
- **SQL 解析**：支持常用 SQL 语句的解析和执行。

## 功能描述

### 1. 数据存储

LiteSQL 通过简单的文件系统模拟数据库数据存储，支持 **分页管理** 和 **数据项抽象**，使用 `DataManager` 管理数据的读取和插入。

### 2. 事务管理

通过 `TransactionManager` 管理事务的生命周期，确保事务的 ACID 特性，包括支持事务的开始、提交、回滚和事务状态查询。

### 3. MVCC（多版本并发控制）

实现了 **多版本并发控制**，使用版本链和事务的版本号来管理并发事务的可见性，支持两种事务隔离级别：**Read Committed (RC)** 和 **Repeatable Read (RR)**。

### 4. 索引系统

基于 **B+ 树** 构建索引系统，支持高效的范围查找与定位，在大量数据查询时，查询响应时间大幅减少。

### 5. 死锁检测

实现了基本的 **死锁检测机制**，通过监控事务的锁依赖关系，避免死锁发生，及时回滚其中一个事务。

### 6. SQL 解析

支持 SQL 语句的基本解析，能够解析并执行常见的 SQL 语句（如 `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `DROP` 等）。

## 安装

### 克隆仓库

```bash
git clone https://github.com/Sand-form/LiteSQL.git
cd LiteSQL

