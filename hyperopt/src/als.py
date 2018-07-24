#!/usr/bin/python
# encoding: utf-8

# Example call from SMAC root:
# python als.py 0 0 0 0 0 -spark.memory.fraction 0.4
# ./smac --use-instances false --numberOfRunsLimit 11 --pcs-file example_scenarios/spark-als/params.pcs --algo "python example_scenarios/spark-als/als.py" --run-objective RUNTIME
# ./smac --scenario-file example_scenarios/spark-als/als-scenario.txt

import sys, os, time, re
from subprocess import Popen, PIPE

import Command


def copyFile(source, target):
    open(target, "w").write(open(source, "r").read())
def appendConfig(target, configs):
    open(target, "a").write(configs)

defaultConfPath = "/home/cloud/bayes_op/smac-v2.10.03-master-778/example_scenarios/spark-als/spark.conf"
targetConfPath = "/home/cloud/HiBench-master/conf/spark.conf"
cmd = "/home/cloud/HiBench-master/bin/workloads/ml/als/spark/run.sh"
print(cmd)

def objective(args):
    ans = 0.0
    try:
        ans = Command.getThrought_smac(params)
    except Exception as e:
        print(e)
        status = "CRASHED"
        return status,ans
    status = "SUCCESS"

    return status, ans

params = sys.argv[6:]
print(params)
ans = 0.0
status,ans = objective(params)
print(status + " " + ans)
print("Result for SMAC: %s, 0, 0, %f, 0" % (status, ans))