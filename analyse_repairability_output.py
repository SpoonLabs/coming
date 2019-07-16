"""
Helps in analysing json produced by JSONRepairabilityOutput

python analyse_repairability_output.py <path to the json>
OR
python analyse_repairability_output.py <path to the json> <path to patches>
"""

import json
import os
import sys

file_name = "./coming_data/jsons/1.json"


def inc(x, y):
    try:
        x[y] += 1
    except KeyError:
        x[y] = 1
    return x


def print_counts(file_name):
    with open(file_name) as f:
        res = json.load(f)

    instances = res['instances']

    tool_count = {}
    pattern_count = {}
    total_count = 0

    for instance in instances:
        repair_instances = instance['repairability']
        for repair_instance in repair_instances:
            tool_name = repair_instance['tool-name']
            pattern_name = repair_instance['pattern-name']
            total_count += 1
            inc(tool_count, tool_name)
            inc(pattern_count, pattern_name)

    print ("Total Counts")
    print (total_count)
    print ("Tool Counts")
    print (tool_count)
    print ("Pattern Counts")
    print (pattern_count)


def print_diff(folder_name, patch_name):
    base_folder = os.path.abspath(os.path.join(folder_name, patch_name, patch_name)) + "/"
    s = base_folder + patch_name + "_" + patch_name + "_s.java"
    t = base_folder + patch_name + "_" + patch_name + "_t.java"
    os.system("diff " + s + " " + t)

def print_undetected_patched(folder_name, file_name):
    with open(file_name) as f:
        res = json.load(f)

    dirs = os.listdir(folder_name)
    patches_found = set()

    instances = res['instances']
    for instance in instances:
        patch_name = instance['revision']
        patches_found.add(patch_name)

    not_found_count = 0

    for dir in dirs:
        if dir not in patches_found:
            print (dir)
            print_diff(folder_name, dir)
            not_found_count += 1

    print ("Unable to find %d instances out of %d " % (not_found_count, len(dirs)))

if __name__ == '__main__':

    file_name = sys.argv[1]
    if len(sys.argv) == 2:
        print_counts(file_name)
    elif len(sys.argv) == 3:
        folder_name = sys.argv[2]
        print_undetected_patched(folder_name, file_name)
        print_counts(file_name)
