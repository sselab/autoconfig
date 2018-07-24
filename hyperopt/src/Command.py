import Config
import os
import Excel
import re
import RemoteSSH
import time
import traceback
from subprocess import Popen, PIPE
# IP
kafka1 = '192.168.0.91'
kafka2 = '192.168.0.92'
kafka3 = '192.168.0.93'
kafkaList = [kafka1, kafka2, kafka3]

producerIP = '192.168.0.178'

# producerIP ='192.168.0.178'
# COMMAND
start_command = 'cd /home; kafka-server-start.sh -daemon  server.properties'
stop_command = 'kafka-server-stop.sh'
create_topic_command = 'kafka-topics.sh --create --topic test --zookeeper localhost:2181 --replication-factor 2 --partitions 3'
list_topic_command = 'kafka-topics.sh --zookeeper localhost:2181 --list'
delete_topic_command = 'kafka-topics.sh --delete --zookeeper localhost:2181  --topic test'
start_producer_command = 'cd /home; java -jar kafkaStudy.jar --topic test --num-records 5000000 --record-size 100 --num-threads 1 --producer.config producer.properties'
stop_producer_command = 'cd /usr/local/kafka; ./stopProducer.sh'

'''
3producer ack-1
--num-records 6000000 --record-size 100
--num-records 1000000 --record-size 1000
--num-records 80000 --record-size 10000
1producer ack-1
--num-records 5000000 --record-size 100
--num-records 600000 --record-size 1000
--num-records 60000 --record-size 10000
3producer ack1
--num-records 15000000 --record-size 100
--num-records 2500000 --record-size 1000
--num-records 300000 --record-size 10000
1producer ack1
--num-records 5000000 --record-size 100
--num-records 1000000 --record-size 1000
--num-records 100000 --record-size 10000
'''
start_consumer_command_prefix = 'cd /script; nohup ./consumerStart.sh'
start_consumer_command_postfix = '>/dev/null 2>&1  &'
stop_consumer_command = 'cd /script; ./consumerStop.sh'

# FILEPATH
producer_property_file_path = './up/producer.properties'
remote_producer_property_file_path = '/home/producer.properties'
server_property_file_path = './up/server.properties'
remote_server_property_file_path = '/home/server.properties'
default_log_path = './out/log.txt'

# WAIT SECOND
wait = 10
# 超时时间
sleeptime = 90

global params
global sample_id


def upload_conf():

    for index,val in enumerate(kafkaList):
        # 创建kafka1配置文件
        Config.save_server_property(broker_id=str(index), ip=val, params=params)
        # 上传kafka1配置文件至服务器
        RemoteSSH.sftp_upload_file(hostname=val, local_path=server_property_file_path,
                               remote_dir=remote_server_property_file_path)
    # 创建生产者配置文件cd
    Config.save_producer_property(params=params)
    # 上传生产者配置文件
    RemoteSSH.sftp_upload_file(hostname=producerIP, local_path=producer_property_file_path,
                               remote_dir=remote_producer_property_file_path)

def start_kafka():
    # 开启kafka
    for index, val in enumerate(kafkaList):
        RemoteSSH.ssh_cmd(val, command=start_command, print_answer=False)
    # 设置超时
    total_time = sleeptime
    # 是否启动
    time.sleep(wait*3)
    while not (judge_state(kafka1, match_str='Kafka') and judge_state(kafka2, match_str='Kafka')and judge_state(kafka3, match_str='Kafka')):
        if total_time == 0:
            break
        time.sleep(wait)
        total_time -= wait
        for index, val in enumerate(kafkaList):
            RemoteSSH.ssh_cmd(val, command=start_command, print_answer=False)
    if total_time == 0:
        dump_log(' kafka start fail')
        raise Exception



def stop_kafka():
    # 关闭kafka
    for index, val in enumerate(kafkaList):
        RemoteSSH.ssh_cmd(val, command=stop_command, print_answer=False)
    # 设置超时
    total_time = sleeptime
    # 是否关闭
    time.sleep(wait)
    while judge_state(kafka1, match_str='Kafka') or judge_state(kafka2, match_str='Kafka')or judge_state(kafka3, match_str='Kafka'):
        if total_time == 0:
            break
        time.sleep(wait)
        total_time -= wait
        for index, val in enumerate(kafkaList):
            RemoteSSH.ssh_cmd(val, command=stop_command, print_answer=False)
    if total_time == 0:
        dump_log(' kafka stop fail')
        raise Exception


def create_topic():
    # 创建topic
    RemoteSSH.ssh_cmd(kafka1,
                      command=create_topic_command,
                      print_answer=True)
    # 设置超时时间
    totaltime = sleeptime
    # 是否创建
    time.sleep(wait)
    # topiclist = RemoteSSH.ssh_cmd(kafka1, command=list_topic_command, print_answer=True)
    while not judge_state(hostname=kafka1, match_str='test', command=list_topic_command):
        if totaltime == 0:
            break
        time.sleep(wait)
        totaltime -= wait
    if totaltime == 0:
        dump_log(' create topic  fail')
        raise Exception


def delete_topic():
    # 删除topic
    RemoteSSH.ssh_cmd(kafka1, command=delete_topic_command, print_answer=True)
    # 设置超时时间
    total_time = sleeptime
    # 是否删除
    time.sleep(wait)
    # topiclist = RemoteSSH.ssh_cmd(kafka1, command=list_topic_command, print_answer=True)
    while judge_state(hostname=kafka1, match_str='test', command=list_topic_command):
        if total_time == 0:
            break
        time.sleep(wait)
        total_time -= wait
        RemoteSSH.ssh_cmd(kafka1, command=delete_topic_command, print_answer=True)
    if total_time == 0:
        dump_log(' delete topic  fail')
        raise Exception


def start_producer():
    # 启动生产者
    str = RemoteSSH.ssh_cmd(producerIP, command=start_producer_command, print_answer=True, time=600)
    return str;

"""
关闭超时的生成者
"""
def stop_producer():
    str = RemoteSSH.ssh_cmd(producerIP, 'jps', print_answer=True)
    stop_list=[]
    for s in str.split('\n'):
        list = s.split(' ')
        if list[1] == 'jar':
            RemoteSSH.ssh_cmd(producerIP, 'kill ' + list[0], print_answer=False)
            stop_list.append(list[0])
    # 设置超时时间
    total_time = sleeptime
    # 是否关闭
    time.sleep(wait)
    while judge_state(producerIP, match_str='jar'):
        if total_time == 0:
            break
        time.sleep(wait)
        total_time -= wait
        for pid in stop_list:
            RemoteSSH.ssh_cmd(producerIP, 'kill ' + pid, print_answer=False)
    if total_time == 0:
        dump_log(' sample_id ' + str(sample_id) + 'stop timeout producer fail')
        raise Exception

def start_local_producer():
    cmd = 'java -jar  kafkaStudy.jar --topic test --num-records 3000000 --record-size 500 --num-threads 1 --producer.config producer.properties'
    p = Popen(cmd, shell=True, stdout=PIPE, stderr=PIPE, cwd='D:\\PycharmProjects\\KafkaPython\\up')
    out = p.stdout.readlines()
    try:
        return out[0].decode('gb2312')
    except Exception as e:
        return 'err'
    # # 启动生产者
    # str = RemoteSSH.ssh_cmd(producerIP, command=start_producer_command, print_answer=True,time=600)
    # return str;

def stop_consumer():
    # 关闭消费者
    RemoteSSH.ssh_cmd(kafka1, command=stop_consumer_command, print_answer=True)
    # 设置超时时间
    total_time = sleeptime
    # 是否关闭
    time.sleep(wait)
    while judge_state(hostname=kafka1, match_str='ConsoleConsumer'):
        if total_time == 0:
            break
        time.sleep(wait)
        total_time -= wait
    if total_time == 0:
        dump_log(' sample_id ' + str(sample_id) + 'stop consumer fail')
        raise Exception


# 刷新参数
def refresh_param_hyperopt(param):
    global  params
    params={}
    #broker
    params['num.network.threads'] = str(param[0]+1)
    params['num.io.threads'] = str(param[1]+1)
    params['queued.max.requests'] = str((param[2]+1)*50)
    params['num.replica.fetchers'] = str(param[3]+1)
    params['socket.receive.buffer.bytes'] = str((param[4]+1)*10240)
    params['socket.send.buffer.bytes'] = str((param[5]+1)*10240)
    params['socket.request.max.bytes'] = str((param[6]+1)*10485760)
    #producer
    params['buffer.memory'] = str((param[7]+1)*2097152)#2M->96M
    params['batch.size'] = str((param[8]+1)*4096)#4k->256K
    params['linger.ms'] = str(param[9]*10)#0->1000
    params['compression.type'] = str(param[10])
    params['acks'] = str(param[11])
    dump_log("param "+str(param))
    dump_log(str(params))

def refresh_param_svm(param):
    global  params
    params={}
    #broker [3, 8, 10, 1, 10, 10, 10, 16, 4, 0, 'none', 1]
    params['num.network.threads'] = str(param[0])
    params['num.io.threads'] = str(param[1])
    params['queued.max.requests'] = str(param[2]*50)
    params['num.replica.fetchers'] = str(param[3])
    params['socket.receive.buffer.bytes'] = str(param[4]*10240)
    params['socket.send.buffer.bytes'] = str(param[5]*10240)
    params['socket.request.max.bytes'] = str(param[6]*10485760)
    #producer
    params['buffer.memory'] = str(param[7]*2097152)#2M->96M
    params['batch.size'] = str(param[8]*4096)#4k->256K
    params['linger.ms'] = str(param[9]*10)#1->1000
    params['compression.type'] = str(param[10])
    params['acks'] = str(param[11])

    #params['log.flush.interval.messages'] = str(param[12]*200)
    #params['log.flush.interval.ms'] = str(param[13]*50)
    dump_log("param " + str(param))
    dump_log(str(params))

def show_param():
    global params
    print(params)


def dump_log(content, flag='old', log_path=default_log_path):
    if flag == 'new':
        os.remove(log_path)
    if not os.path.exists(log_path):
        f = open(log_path, 'w')
        f.write(time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())) + ' ' + content + '\n')
        f.close()
    else:
        f = open(log_path, 'a')
        f.write(time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())) + ' ' + content + '\n')
        f.close()

"""
命令结果判断
"""
def judge_state(hostname, match_str, occurrence=-1, command='jps'):
    return_str = ''
    try:
        return_str = RemoteSSH.ssh_cmd(hostname, command, print_answer=True)
    except:
        traceback.print_exc()
    if occurrence != -1:
        if return_str.count(match_str) == occurrence:
            return True
        else:
            return False
    else:
        if return_str.find(match_str) != -1:
            return True
        else:
            return False


def getThrought(args,type='svm'):

    #refresh_param_hyperopt(args)
    if type=='svm':
        refresh_param_svm(args)
    else:
        refresh_param_hyperopt(args)
    # show_param()
    upload_conf()
    start_kafka()
    create_topic()

    # remote
    ans = start_producer()
    # ans = start_local_producer()
    ans.strip()
    ans.replace('\r\n', '')
    ans = " ".join(ans.split())
    dump_log('answear '+ ans)
    if ans=='err':
        stop_producer()
    delete_topic()
    stop_kafka()
    #remote ans = ans.split(',')[1].split(' ')[0]
    searchObj = re.search(r'(\((.*?) MB/sec\)), ((.*?) ms avg latency)', ans)
    t=0.01
    l=1000
    if ans!='err':
        t = searchObj.group(2)
        l = searchObj.group(4)
        # ans = ans.split(',')[2].split(' ')[0]
        # ans = ans.replace('(', '')
        # ans=-1.0;
    t=float(t)
    l=float(l)
    print(t)
    print(l)
    return t

if __name__ == "__main__":
    # for i in range(5):
    #     p = [2, 7, 9, 0, 9, 9, 9, 31, 1, 0, 'none', 1]
    #     getThrought(p)
    #专家
    # for i in range(5):
    #     p = [5, 11, 9, 0, 99, 99, 9, 63, 7, 10, 'lz4', 1]
    #     getThrought(p)
    #单partition
    # for i in range(5):
    #     p =[18, 19, 70, 5, 12, 14, 21, 90, 63, 69, 'none', 0]
    #     getThrought(p)
    #多partition
    # for i in range(5):
    #     p = [10, 1, 14, 6, 18, 13, 29, 17, 49, 49, 'none', 0]
    #     getThrought(p)
    # stop_producer()
    # 1000byte
    #default
    #[2, 7, 9, 0, 9, 9, 9, 31, 1, 0, 'none', 1]
    #专家
    #[5, 11, 9, 0, 99, 99, 9, 63, 7, 10, 'lz4', 1]
    #单 partition
    #[9, 22, 55, 1, 13, 13, 7, 27, 47, 40, 'lz4', 1]
    #多 partition
    #[7, 11, 86, 0, 16, 14, 16, 6, 30, 70, 'none', 0]

    # p = [2, 7, 9, 0, 9, 9, 9, 31, 1, 0, 'none', 1]
    # 默认值 ！+1


    # 500组实验最快 ！+1
    # p = [10, 3, 14, 6, 18, 13, 29, 17, 49, 49, 'none', 0]
    # p=[14,15,47,3,22,27,4,7,84,98,'none',0]
    # 500组实验最慢 ！=1
    # p=[4,1,20,1,9,7,24,48,2,87,'snappy',-1]
    # hyperopt +1

    # p=(5, 23, 28, 13, 10, 12, 0, 8, 61, 44, 'lz4', -1)


    # p = [20,14,86,11,0,10,15,12,57,31, 'none', 1]
    # getThrought(p)

    # p = [10,12,86,12,16,15,25,12,25,53, 'none', 1]
    # getThrought(p)
    # p= [13,20,76,11,2,10,18,18,47,27,'none',1]
    # getThrought(p)

    delete_topic()
    # create_topic()
    stop_kafka()
    # start_kafka()




    # p = [8, 3, 26, 8, 18, 9, 9, 16, 50, 61, 'none', 1]
    # for i in range(1):
    #     getThrought(p)
    # # #best
    # p=[16, 8, 51, 12, 6, 17, 7, 9, 61, 72, 'none', 1]
    # for i in range(1):
    #     getThrought(p)
    # #hyperopt
    # p=(15, 21, 0, 14, 18, 15, 15, 34, 49, 60, 'none', 1)
    # for i in range(1):
    #     getThrought(p,type='hyperopt')
    # #random
    # p=[4 ,13, 49, 2, 20, 20, 30, 48, 63, 71, 'none', 1]
    # for i in range(1):
    #     getThrought(p)
    # #GA
    # p=[10,12,43,2,19,19,29,48,63,78,'none',1]
    # for i in range(1):
    #     getThrought(p)
    # #default
    # p = [3, 8, 10, 1, 10, 10, 10, 16, 4, 0, 'none', 1]
    # for i in range(2):
    #     getThrought(p)
    # p=[5 ,13, 94, 17, 19, 10, 2 ,38 ,55 ,78, 'gzip', 1]
    # getThrought(p)



    #tree+ga

    # p = [10	,8	,28	,24	,5	,14,	20,	8,	71,	49,	'none'	,1]

    # for i in range(2):
    #     getThrought(p)
    # p = [19, 21, 57, 7, 7, 16, 15, 47, 42, 21, 'none', 1]
    # for i in range(2):
    #     getThrought(p)
    # hyperopt
    # p= [6, 9, 36, 2, 6, 4, 14, 9, 63, 16, 'none', 1]
    # getThrought(p)
    # p = [19, 11, 69, 1, 20, 16, 17, 14, 28, 82, 'lz4', 0]
    # getThrought(p)
    # getThrought_smac(p)

