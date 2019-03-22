#coding=utf-8
import os
import sys
import time
import re

import datetime
import pandas as pd
import Command
import random
import socket
import math
import json
def main():
    params = sys.argv[6:]
    try:
            Command.refresh_param(params)
            Command.show_param()
            Command.upload_conf()
            Command.start_kafka()
            Command.create_topic()
            Command.start_consumer()

            Command.stop_consumer()
            Command.stop_producer()
            Command.delete_topic()
            Command.stop_kafka()
    except Exception as e:
            Command.dump_log(str(e))
def socket_java(str,type='bestconfig'):
    ziplist = ['none', 'gzip', 'snappy', 'lz4']
    param = json.loads(str)
    # param = json.loads('{"socket.request.max.bytes":6.690755,"compression.type":0.871446,"performance":null,'
    #                    '"num.network.threads":19.055355,"num.io.threads":12.78569,"socket.receive.buffer.bytes":8.650538,'
    #                    '"batch.size":21.0,"queued.max.requests":4.0,"num.replica.fetchers":2.082252,"buffer.memory":44.0,'
    #                    '"socket.send.buffer.bytes":8.855171,"linger.ms":17.0}')
    p = []
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
    if type =='bestconfig':
        p.append(ziplist[math.floor(param['compression.type'] * 4)])
    else:#oals
        p.append(param['compression.type'])
    p.append(1)
    print(p)
    return Command.getThrought(p)
def random_parameter():
    params={}
    params['num.network.threads'] = random.randint(1, 20)#1-20
    params['num.io.threads'] = random.randint(1, 24)#1-24
    params['queued.max.requests'] = random.randint(1, 100)# * 50#50-5000
    params['num.replica.fetchers'] = random.randint(1, 20)#1-6
    params['socket.receive.buffer.bytes'] = random.randint(1, 20)# * 10240#10K-200K
    params['socket.send.buffer.bytes'] = random.randint(1, 20)# * 10240#10K-200K
    params['socket.request.max.bytes'] = random.randint(1, 30)# * 10485760#10M-300M
    #producer
    params['buffer.memory'] = random.randint(1, 48)#* 2097152 # 1M->96M
    params['batch.size'] = random.randint(1, 64)# * 4096 # 1k->512K
    params['linger.ms'] = random.randint(0, 100)# * 10#0-1000
    type=['none', 'gzip', 'snappy', 'lz4']
    params['compression.type'] = random.choice(type)
    acks=['1']
    params['acks'] = random.choice(acks)

    #params['log.flush.interval.messages'] = random.randint(1, 150)  # *200 #200-30000
    #params['log.flush.interval.ms'] = random.randint(1, 100)  # *50#50-5000
    #Command.dump_log()
    return params
def dump_csv(content, flag='old', log_path='./out/log.csv'):
    if flag == 'new':
        os.remove(log_path)
    if not os.path.exists(log_path):
        f = open(log_path, 'w')
        f.write(content + '\n')
        f.close()
    else:
        f = open(log_path, 'a')
        f.write(content + '\n')
        f.close()

def  create_sample_csv():
    """
    生成样本csv
    :return: 
    """
    # dump_csv(params[],flag='new')
    title = random_parameter().keys()
    title = ','.join(title)
    print(title)
    dump_csv(title)

    # data = list(random_parameter().values())
    # print(",".join(str(n) for n in data))
    for i in range(500):
        data = list(random_parameter().values())
        dump_csv(",".join(str(n) for n in data))

def start_experiment():
    df = pd.read_csv('./out/log.csv', engine='python')
    starttime = time.time()
    for indexs in df.index:
        interval = time.time() - starttime
        if interval<360000:
            # print(indexs)
            try:
                p = df.loc[indexs].values
                Command.getThrought(p)
            except Exception as e:
                continue

starttime = time.time()
def extract_result():
    file = open("./out/log.txt")
    for line in file:
       if 'answear' in line:
           searchObj = re.search(r'(\((.*?) MB/sec\)), ((.*?) ms avg latency)', line)
           # print(line[27:].split(',')[2].strip().split(' ')[0].replace('(', ''))
           # print(line[27:].split(',')[3].strip().split(' ')[0])
           # print(searchObj.group(2))
           # print(searchObj.group(4))
           if 'err' in line:
                 dump_csv('null', log_path='./out/result.csv')
           else:
                 dump_csv(searchObj.group(2)+','+searchObj.group(4), log_path='./out/result.csv')
    file.close()
def br():
    if(time.time()-starttime>10):
        sys.exit(0)
    print(time.time())
    time.sleep(2)
def listen_socket(type='bestconfig'):
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM);
        print("create socket succ!")
        sock.bind(('localhost', 8889))
        print('bind socket succ!')
        sock.listen(1)
        print('listen succ!')
    except:
        print("init socket error!")

    while True:
        print("listen for client...")
        conn, addr = sock.accept()
        print("get client")
        print(addr)
        conn.settimeout(10000)

        szBuf = conn.recv(2048)
        print("recv:" + str(szBuf, 'utf-8'))
        ans = socket_java(szBuf,type)
        conn.send(bytes(str(ans), 'utf-8'))

        conn.close()
        print("end of servive")
def top_throughput():
    file = open("./out/log.txt")
    max =-1
    maxparam=''
    line = file.readline()
    start = datetime.datetime.strptime(line[0:19], "%Y-%m-%d %H:%M:%S")
    file.seek(0)
    while (file):
        #get time
        line = file.readline()
        if line =='':
            break
        cur = datetime.datetime.strptime(line[0:19], "%Y-%m-%d %H:%M:%S")
        params = line[26:]
        #param
        line = file.readline()
        #throughput
        line = file.readline()
        throughput=0.1
        if 'answear' in line and 'err' not in line:
            searchObj = re.search(r'(\((.*?) MB/sec\)), ((.*?) ms avg latency)', line)
            throughput = float(searchObj.group(2))
            if(max<throughput):
                max = throughput
                maxparam = params
        diff = (cur-start).total_seconds()
        if(diff>=3500):
            dump_csv(str(max) + ',' + maxparam.strip(), log_path='./out/top.csv')
            start = cur
    file.close()
if __name__ == "__main__":
    #listen_socket()
    #listen_socket('oals')
    #create_sample_csv()
    start_experiment()
    #extract_result()
    # top_throughput()
    # time.sleep(19000)
    # for i in range(3):
    #     start_experiment()
    # extract_result()

    # line = "2017-12-28 23:15:52 answear 2000000 records sent, 17098.693660 records/sec,(8.15 MB/sec), 882.70 ms avg latency, 1280.00 ms max latency"
    # pattern = re.compile(r'hello')
    # 使用Pattern匹配文本，获得匹配结果，无法匹配时将返回None
    # match = pattern.match('hello world!')1-20
    # create_sample_csv()

