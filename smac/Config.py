import re
import os
import tempfile


server_property_list = ['num.network.threads', 'num.io.threads', 'queued.max.requests', 'num.replica.fetchers',
                        'socket.receive.buffer.bytes', 'socket.send.buffer.bytes', 'socket.request.max.bytes']
producer_property_list = ['buffer.memory', 'batch.size', 'linger.ms', 'compression.type', 'acks']
producer_property_file_path = './up/producer.properties'
server_property_file_path = './up/server.properties'


class Properties:

    def __init__(self, file_name):
        self.file_name = file_name
        self.properties = {}
        try:
            fopen = open(self.file_name, 'r')
            for line in fopen:
                line = line.strip()
                if line.find('=') > 0 and not line.startswith('#'):
                    strs = line.split('=')
                    self.properties[strs[0].strip()] = strs[1].strip()
        except Exception as e:
            raise e
        else:
            fopen.close()

    def has_key(self, key):
        return self.properties.has_key(key)

    def get(self, key, default_value=''):
        if self.properties.has_key(key):
            return self.properties[key]
        return default_value

    def put(self, key, value):
        self.properties[key] = value
        replace_property(self.file_name, key + '=.*', key + '=' + value, True)

    def delete(self, key):
        delete_property(self.file_name, key + '=.*', True)


def parse(file_name):
    return Properties(file_name)


def replace_property(file_name, from_regex, to_str, append_on_not_exists=True):
    file = tempfile.TemporaryFile()         #创建临时文件

    if os.path.exists(file_name):
        r_open = open(file_name,'r')
        pattern = re.compile(r''+from_regex)
        found = None
        for line in r_open: #读取原文件
            if pattern.search(line) and not line.strip().startswith('#'):
                found = True
                line = re.sub(from_regex,to_str,line)
            file.write(line.encode('utf-8'))   #写入临时文件
        if not found and append_on_not_exists:
            file.write(b'\n' + to_str.encode('utf-8'))
            print('props not found')
        r_open.close()
        file.seek(0)

        content = file.read()  #读取临时文件中的所有内容
        if os.path.exists(file_name):
            os.remove(file_name)

        w_open = open(file_name,'w')
        w_open.write(content.decode('utf-8'))   #将临时文件中的内容写入原文件
        w_open.close()

        file.close()  #关闭临时文件，同时也会自动删掉临时文件
    else:
        print ("file %s not found" % file_name)

def delete_property(file_name, from_regex, append_on_not_exists=True):
    file = tempfile.TemporaryFile()         #创建临时文件
    if os.path.exists(file_name):
        r_open = open(file_name,'r')
        pattern = re.compile(r''+from_regex)
        found = False
        for line in r_open: #读取原文件
            found = False
            if pattern.search(line) and not line.strip().startswith('#'):
                found = True
            if not found:
                file.write(line.encode('utf-8'))   #写入临时文件
        r_open.close()
        file.seek(0)
        content = file.read()  #读取临时文件中的所有内容
        if os.path.exists(file_name):
            os.remove(file_name)
        w_open = open(file_name, 'w')
        w_open.write(content.decode('utf-8'))   #将临时文件中的内容写入原文件
        w_open.close()
        file.close()  #关闭临时文件，同时也会自动删掉临时文件
    else:
        print("file %s not found" % file_name)


def save_producer_property(params):
    sample_data = params
    producer_property_file = parse(producer_property_file_path)
    for property in producer_property_list:
        producer_property_file.put(property, sample_data[property])
    # producer_property_file.put('buffer.memory', sample_data['buffer.memory'])
    # producer_property_file.put('batch.size', sample_data['batch.size'])
    # producer_property_file.put('linger.ms', sample_data['linger.ms'])
    # producer_property_file.put('timeout.ms', sample_data['timeout.ms'])
    # producer_property_file.put('message.length', sample_data['message.length'])


def save_server_property(broker_id, ip, params):
    sample_data = params
    server_property_file = parse(server_property_file_path)
    server_property_file.put('broker.id', broker_id)
    server_property_file.put('listeners', 'PLAINTEXT://'+ip+':9092')
    for property in server_property_list:
        server_property_file.put(property, sample_data[property])
    # server_property_file.put('num.network.threads', sample_data['num.network.threads'])
    # server_property_file.put('num.io.threads', sample_data['num.io.threads'])
    # server_property_file.put('queued.max.requests', sample_data['queued.max.requests'])
    # server_property_file.put('num.replica.fetchers', sample_data['num.replica.fetchers'])
    # server_property_file.put('replica.fetch.min.bytes', sample_data['replica.fetch.min.bytes'])
    # server_property_file.put('replica.fetch.max.bytes', sample_data['replica.fetch.max.bytes'])
    # server_property_file.put('replica.fetch.wait.max.ms', sample_data['replica.fetch.wait.max.ms'])
    # server_property_file.put('num.partitions', sample_data['num.partitions'])
    # server_property_file.put('min.insync.replicas', sample_data['min.insync.replicas'])


def main():
    file_name = './up/server.properties'
    from_regex = 'broker.id=.*'
    # save_producer_property(1)
    # props = parse(file_name)  # 读取文件
    # props.put('new', 'abc')       #修改/添加key=value
    # props.put('broker.id', '13')
    # props.put('host.name', '192.168.0.100')
    # props.delete("new")
    # for property in server_property_list:
    #     print(property)

if __name__=="__main__":
    main()