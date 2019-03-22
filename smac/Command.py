import Config
import os
import re
import Excel
import RemoteSSH
import time
import traceback

# IP
kafka1 = '192.168.0.91'
kafka2 = '192.168.0.92'
kafka3 = '192.168.0.93'
kafkaList = [kafka1, kafka2, kafka3]

producerIP = '192.168.0.178'#178
# COMMAND
start_command = 'cd /home; kafka-server-start.sh -daemon  server.properties'
stop_command = 'kafka-server-stop.sh'
create_topic_command = 'kafka-topics.sh --create --topic test --zookeeper localhost:2181 --replication-factor 2 --partitions 3'
list_topic_command = 'kafka-topics.sh --zookeeper localhost:2181 --list'
delete_topic_command = 'kafka-topics.sh --delete --zookeeper localhost:2181  --topic test'
start_producer_command = 'cd /home; java -jar kafkaStudy.jar --topic test --num-records 2000000 --record-size 1000 --num-threads 3 --producer.config producer.properties'
stop_producer_command = 'cd /usr/local/kafka; ./stopProducer.sh'

start_consumer_command_prefix = 'cd /script; nohup ./consumerStart.sh'
start_consumer_command_postfix = '>/dev/null 2>&1  &'
stop_consumer_command = 'cd /script; ./consumerStop.sh'

# FILEPATH
producer_property_file_path = './up/producer.properties'
remote_producer_property_file_path = '/home/producer.properties'
server_property_file_path = './up/server.properties'
remote_server_property_file_path = '/home/server.properties'
log_dir = './out/'
log_path = log_dir + 'log.txt'

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
    str = RemoteSSH.ssh_cmd(producerIP, command=start_producer_command, print_answer=True, time=480)
    return str;

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
def refresh_param(param):
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
    params['buffer.memory'] = str((param[7]+1)*1048576)#1M->96M
    params['batch.size'] = str((param[8]+1)*1024)#1k->128K
    params['linger.ms'] = str(param[9]*50)
    params['compression.type'] = str(param[10])
    params['acks'] = str(param[11])
    dump_log(str(params))

def refresh_param_smac(param):
    global params
    params = {}
    for index,val in enumerate(param):
        if(index%2==0):
            print(param[index][1:]+" "+param[index+1])
            params[str(param[index][1:])] = param[index+1]
    """
    params['num.network.threads'] = str(param[0])
    params['num.io.threads'] = str(param[1])
    params['queued.max.requests'] = str(param[2] * 50)
    params['num.replica.fetchers'] = str(param[3])
    params['socket.receive.buffer.bytes'] = str(param[4] * 10240)
    params['socket.send.buffer.bytes'] = str(param[5] * 10240)
    params['socket.request.max.bytes'] = str(param[6] * 10485760)
    # producer
    params['buffer.memory'] = str(param[7] * 2097152)  # 2M->96M
    params['batch.size'] = str(param[8] * 4096)  # 4k->256K
    params['linger.ms'] = str(param[9] * 10)  # 1->1000
    params['compression.type'] = str(param[10])
    params['acks'] = str(param[11])
    """

    #broker
    # params['num.network.threads'] = str(param[0])
    # params['num.io.threads'] = str(param[1])
    params['queued.max.requests'] = str(int(params['queued.max.requests'])*50)
    # params['num.replica.fetchers'] = str(param[3])
    params['socket.receive.buffer.bytes'] = str(int(params['socket.receive.buffer.bytes'])*10240)
    params['socket.send.buffer.bytes'] = str(int(params['socket.send.buffer.bytes'])*10240)
    params['socket.request.max.bytes'] = str(int(params['socket.request.max.bytes'])*10485760)
    #producer
    params['buffer.memory'] = str(int(params['buffer.memory'])*2097152)#1M->96M
    params['batch.size'] = str(int(params['batch.size'])*4096)#1k->128K
    params['linger.ms'] = str(int(params['linger.ms'])*10)
    # params['compression.type'] = str(param[10])
    # params['acks'] = str(param[11])

    dump_log(str(params))

def show_param():
    global params
    print(params)


def dump_log(content, flag='old'):
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


# 命令结果判断
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

def getThrought_smac(args):
    dump_log("param "+str(args))
    refresh_param_smac(args)
    # show_param()
    upload_conf()
    start_kafka()
    create_topic()

    ans = start_producer()
    ans.strip()
    ans.replace('\r\n', '')
    ans = " ".join(ans.split())
    dump_log('answear ' + ans)
    if ans=='err':
        stop_producer()
    delete_topic()
    stop_kafka()

    searchObj = re.search(r'(\((.*?) MB/sec\)), ((.*?) ms avg latency)', ans)
    t = 0.01
    l = 1000
    if ans != 'err':
        t = searchObj.group(2)
        l = searchObj.group(4)
        # ans = ans.split(',')[2].split(' ')[0]
        # ans = ans.replace('(', '')
        # ans=-1.0;
    t = float(t)
    l = float(l)
    print(t)
    print(l)
    return t
if __name__ == "__main__":
    # stop_kafka()


    # refresh_param(p)

       p=['-acks', '1', '-batch.size', '35', '-buffer.memory', '47', '-compression.type', 'none', '-linger.ms', '99',
          '-num.io.threads', '23', '-num.network.threads', '14', '-num.replica.fetchers', '9', '-queued.max.requests',
          '61', '-socket.receive.buffer.bytes', '12', '-socket.request.max.bytes', '12', '-socket.send.buffer.bytes', '1']
       getThrought_smac(p)
       # getThrought_smac(p)
       # getThrought_smac(p)
    # stop_kafka()
    # create_topic()
    # print(start_producer())
    # 'err'.split(',')[1].split(' ')[0]
    # p = ['-acks', '1', '-batch.size', '16', '-buffer.memory', '32', '-compression.type', 'none', '-linger.ms', '0',
    #  '-num.io.threads', '8', '-num.network.threads', '3', '-num.replica.fetchers', '1', '-queued.max.requests', '10',
    #  '-socket.receive.buffer.bytes', '10', '-socket.request.max.bytes', '10', '-socket.send.buffer.bytes', '10']
    # getThrought_smac(p)

