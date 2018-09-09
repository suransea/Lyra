# coding=utf-8

import argparse
import configparser
import getpass
import re
import readline
import socket
import sys

# Tab 自动补全的字符串
WORDS = ['table', 'select ', 'create ', 'drop ', 'delete from ', 'update ', 'insert into ', 'where ', 'for',
         'from ', 'database', 'show ', 'set ', 'use ', 'varchar(', 'int ', 'values(', 'help',
         'create ', 'user ', 'alter ', 'identified by ', 'by ', 'quit', 'password', 'order by ']

help = '''
SQL (不区分大小写)

DAL:
    * USE <DB_NAME>
    * SHOW DATABASES
    * SHOW TABLES

DCL:
    * CREATE USER <USERNAME> IDENTIFED BY "PASSWORD"
    * ALTER USER <USERNAME> IDENTIFED BY "PASSWORD"

    > 删除用户请使用DELETE操作lyra数据库的user表

DDL:
    * CREATE DATABASE <DB_NAME>
    * CREATE TABLE <TABLE_NAME> (<COLUMN_NAME> <TYPE>[,<COLUMN_NAME> <TYPE>]...)
    * DROP DATABASE <DB_NAME>
    * DROP TABLE <TABLE_NAME>

    > TYPE 目前支持INT与VARCHAR(length)

DML:
    * INSERT INTO <TABLE_NAME>[(<COLUMN_NAME>[,<COLUMN_NAME>]...)] VALUES("VALUE"[,"VALUE"]...)
    * DELETE FROM <TABLE_NAME> [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...]
    * UPDATE <TABLE_NAME> SET <COLUMN_NAME>="VALUE"[,<COLUMN_NAME>="VALUE"]... [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...]

DQL:
    * SELECT {*|<COLUMN_NAME>[,<COLUMN_NAME>]...} FROM <TABLE_NAME> [WHERE <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE" [{AND|OR} <COLUMN>{=|>|<|>=|<=|<>|!=}"VALUE"]...] [ORDER BY <COLUMN>[,<COLUMN>]... [{ASC|DESC}]

'''

for string in WORDS.copy():
    WORDS.append(string.upper())


def completer(text, state):
    options = [word for word in WORDS if word.startswith(text)]
    if state < len(options):
        return options[state]
    else:
        return None


readline.parse_and_bind('tab:complete')
readline.set_completer(completer)

if __name__ == '__main__':
    # 解析命令行参数
    parser = argparse.ArgumentParser(description='lyra database monitor.', add_help=False)
    parser.add_argument('-h', '--host', default='127.0.0.1', help='the server IP address')
    group = parser.add_argument_group()
    group.add_argument('-u', '--username', default='root', help='your username')
    group.add_argument('-p', '--password', action='store_true', default=False, help='if the user has a password')
    args = parser.parse_args()
    username = args.username
    password = '(none)'
    host = args.host
    if args.password:
        password = getpass.getpass('Enter password:')

    if password == '':
        password = '(none)'

    # 解析配置文件
    cf = configparser.ConfigParser()
    cf.read('./lyra.conf', 'utf-8')
    port = cf.getint('host', 'port')

    # 创建socket连接服务器
    sk = socket.socket()
    try:
        sk.connect((host, port))
    except socket.error as e:
        print(e.strerror)
        sys.exit(1)

    # 登录
    login = 'login ' + username + ' ' + password + '\n'
    try:
        sk.sendall(login.encode('utf-8'))
        rev = sk.recv(1024).decode('utf-8')
    except socket.error as e:
        print(e.strerror)
        sys.exit(1)

    status = re.findall(r'\S+', rev)
    if status[0] == 'access':
        version = status[1]
        count = status[2]
    else:
        print('Username or password is not right.')
        sys.exit(1)

    print('\nWelcome to the Lyra monitor.  Commands end with ; .')
    print('Your connection id is ' + count)
    print("Server version: " + version)
    print()
    print("Type 'help;' or '\\h' for help. Type '\\c' to clear the current input statement.\n\n")

    cur_db = '(none)'
    sql = ''
    line = 0
    cmd = input('lyra [' + cur_db + '] > ')
    while True:
        line += 1
        if cmd in ['quit', 'quit;', 'exit', 'exit;'] and line == 1:
            print('\nBye.')
            sys.exit(0)
        if cmd == r'\c':
            sql = ''
            line = 0
            cmd = input('lyra [' + cur_db + '] > ')
            continue
        if cmd in [r'\h', 'help', 'help;']:
            print(help)
            sql = ''
            line = 0
            cmd = input('lyra [' + cur_db + '] > ')
            continue
        if cmd.endswith(';'):
            sql += cmd
            try:
                sk.sendall('sql\n'.encode('utf-8'))
                sk.sendall((sql + '\n').encode('utf-8'))
                rev = ''
                while True:
                    data = sk.recv(8192)
                    if not data:
                        break
                    rev += data.decode('utf-8')
                    if rev.endswith('true\n') or rev.endswith('false\n'):
                        break
            except socket.error as e:
                print(e.strerror)
                sys.exit(1)
            complete = False
            if rev.endswith('true\n'):
                complete = True
            print()
            print(rev.rstrip('false\n').rstrip('true\n'))
            print()
            sub_sql = re.findall(r'\w+', sql)
            if sub_sql[0:3] == ['drop', 'database', cur_db] and complete:
                cur_db = '(none)'
            elif sub_sql[0] == 'use' and complete:
                cur_db = sub_sql[1]
            sql = ''
            line = 0
            cmd = input('lyra [' + cur_db + '] > ')
        else:
            sql += cmd + ' '
            cmd = input('    -> ')
