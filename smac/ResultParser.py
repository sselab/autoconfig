import os
import datetime
import time
global throught_now
global time_before
# path = 'F:/Kafka/Kafka实验/crawler/kafaka/result1'


def get_data(excel_path):
    global throught_now
    global time_before
    list =[]
    time_before = datetime.datetime.strptime(os.listdir(excel_path)[0], '%Y-%m-%d %H-%M-%S')
    print(time_before)
    throught_now = ''
    # lib = open(file_name, 'r')
    # for row in tables:
    #     print(row)
    # print(tables[0])
    # print(tables[0]['num.io.threads'])
    # tables = excel_table_byname()
    # for row in tables:
    #     print(row)
    filepath = os.path.join(excel_path, os.listdir(excel_path)[0])
    fopen = open(filepath, 'r')
    list.append(round(float(fopen.readline()), 3))
    for allDir in os.listdir(excel_path):
        print(allDir)
        time = datetime.datetime.strptime(allDir, '%Y-%m-%d %H-%M-%S')
        print((time - time_before).seconds)
        if (time - time_before).seconds > 120:
            filepath = os.path.join(excel_path, allDir)
            fopen = open(filepath, 'r')
            content = fopen.readline()
            print(content)
            print(round(float(content), 3))
            list.append(round(float(content), 3))
        # filepath = os.path.join(path, allDir)
        # print(filepath)
        # fopen = open(filepath, 'r')
        # throught = fopen.readline()
        # throught = throught[0:8]
        # print(throught)
        # if throught == throught_now:
        #     continue
        # else:
        #     list.append(throught)
        # throught_now = throught
        time_before = time
    print(list)
    print(len(list))
    return list


def write_file(data, file_name):
    f = open(file_name, 'w')
    for line in data:
        f.write( str(line) + '\n')
    f.close()

if __name__ == "__main__":

    path = 'F:/Kafka/Kafka实验/crawler/kafaka/result3核4G'
    get_data(path)
    # write_file(get_data(path),'./data.txt')
