import os
import datetime
import xlwt
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


def write_excel(data, file_name):
    row_num = 0
    workbook = xlwt.Workbook()
    sheet = workbook.add_sheet("1")
    for row_data in data:
        sheet.write(row_num, 1, row_data)
        row_num += 1
    workbook.save(file_name)

if __name__ == "__main__":
    # path = 'F:/Kafka/Kafka实验/crawler/kafaka/all_data1'
    # for allDir in os.listdir(path):
    #     print(allDir)
    #     write_excel(get_data(os.path.join(path, allDir)), './'+allDir+'.xlsx')

    path = 'F:/Kafka/Kafka实验/crawler/kafaka/result'
    # get_data(path)
    write_excel(get_data(path), './data.xlsx')
    # get_data(path)
    # write_excel(get_data(), './data1.xlsx')