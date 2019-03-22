#coding=utf-8

import sys
import Command


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


if __name__ == "__main__":
    main()
