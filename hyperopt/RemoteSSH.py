import   paramiko
import   os
import traceback
import Command
# hostname = '192.168.0.91'
username = 'root'
password = '123456'
port = 22
# local_dir = './up'
# remote_dir = '/usr/local/'


def ssh_cmd(hostname, command, print_answer=False, time=None):
    return_str = ''
    print('command: '+command)
    try:
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(hostname=hostname, username=username, password=password, timeout=300)
        # ssh.exec_command(command)
        if print_answer:
            stdin, stdout, stderr = ssh.exec_command(command, timeout=time)
            return_str = stdout.read().decode('utf-8')
            return_str = return_str.rstrip()
            print('return_str: \n'+return_str)
            return_err = stderr.read().decode('utf-8')
            if len(return_err) != 0:
                print('return_err: \n'+return_err)
                return_str = 'err'
        else:
            ssh.exec_command(command)
        ssh.close()
    except Exception:
        traceback.print_exc()
        ssh.close()
        return_str = 'err'
        return return_str
    return return_str

def sftp_upload_dir(hostname,local_dir,remote_dir):
    try:
        t = paramiko.Transport(hostname,port)
        t.connect(username = username, password = password)
        sftp = paramiko.SFTPClient.from_transport(t)
        files = os.listdir(local_dir)
        for f in files:
            sftp.put(os.path.join(local_dir,f),os.path.join(remote_dir,f))
        t.close()
    except:
        traceback.print_exc()

def sftp_upload_file(hostname, local_path,remote_dir):
    try:
        t = paramiko.Transport(hostname, port)
        t.connect(username=username, password=password)
        sftp = paramiko.SFTPClient.from_transport(t)
        sftp.put(local_path, remote_dir)
        t.close()
    except:
        traceback.print_exc()

def main():
    # uploadfile()
    print()
    # ssh_cmd('192.168.0.91','jps',print_answer=True)
    # ssh_cmd(Command.zookeeper1, command=Command.stop_producer_command, print_answer=True)
    # ssh_cmd(Command.kafka1, command=Command.stop_command, print_answer=False)
    # ssh_cmd(Command.kafka1, command=Command.stop_consumer_command, print_answer=True)
    # ssh_cmd(Command.kafka1, command='cd /script; nohup ./consumerStart.sh 2 >/dev/null 2>&1  &', print_answer=True)
    # ssh_cmd(Command.kafka1, command=Command.start_consumer_command + ' 2', print_answer=False)
    # sftp_upload_file(hostname='192.168.0.91', local_path='./up/server.properties', remote_dir='/usr/local/server.properties')
if __name__=="__main__":
    main()