import os

import re
import json
import math
import Command
a = 3
import struct
from subprocess import Popen, PIPE
def Fuc():
    print('func')
    raise Exception
    print('end')
if __name__ == "__main__":
    # s1 = '123213415as test-topic \n asdfsaf128934 12test-topicasda'
    # print(s1.count('xx'))
    #cmd = 'java -jar  multi.jar --topic test --num-records 100000 --record-size 1000 --producer.config producer.properties'
    # cmd ='java -version'
    #D:\PycharmProjects\KafkaPython\\up\
    # for i in range(10):
    #     Fuc()
    #     print('hello')
    # print(a)

    # p = Popen(cmd, shell=True, stdout=PIPE, stderr=PIPE, cwd='D:\\PycharmProjects\\KafkaPython\\up')
    # out = p.stdout.readlines()
    # o2 = p.stderr.readlines()
    # print(len(out))
    # print(len(o2))
    # for l in out:
    #     print(l.decode('gb2312'))
    # ans = out[0].decode('gb2312')
    # ans = ans.split(',')[2].split(' ')[0]
    # ans = ans.replace('(', '')
    # print(ans)
    # print(out)#[1].decode('gb2312')
    # s = 'hello world(my)'
    # reg = r'\((.+)\)'
    # arr = re.findall(reg, s)
    # print(arr)
    ziplist=['none', 'gzip', 'snappy', 'lz4']
    param = json.loads('{"socket.request.max.bytes":6.690755,"compression.type":0.871446,"performance":null,'
                   '"num.network.threads":19.055355,"num.io.threads":12.78569,"socket.receive.buffer.bytes":8.650538,'
                   '"batch.size":21.0,"queued.max.requests":4.0,"num.replica.fetchers":2.082252,"buffer.memory":44.0,'
                   '"socket.send.buffer.bytes":8.855171,"linger.ms":17.0}')
    p=[]
    print(param['socket.request.max.bytes'])
    p.append(math.floor(param['num.network.threads']))
    p.append(math.floor(param['num.io.threads']))
    p.append(math.floor(param['queued.max.requests']))
    p.append(math.floor(param['num.replica.fetchers']))
    p.append(math.floor(param['socket.receive.buffer.bytes']))
    p.append(math.floor(param['socket.send.buffer.bytes']))
    p.append(math.floor(param['socket.request.max.bytes']))
    p.append(math.floor(param['buffer.memory']))
    p.append(math.floor(param['batch.size']))
    p.append(math.floor(param['linger.ms']))
    p.append(ziplist[math.floor(param['compression.type']*4)])
    p.append(1)
    print(p)
    #Command.getThrought(p)
    a=66
    print(bytes(str(a),'utf-8'))
    # s = '[通配符]你好，今天开学了{通配符},你好'
    # print("s", s)
    # a1 = re.compile(r'\[.*?\]')
    # d = a1.sub('', s)
    # print("d", d)
    # a1 = re.compile(r'\{[^}]*\}')
    # d = a1.sub('', s)
    # print("d", d)
