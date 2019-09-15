#!/usr/bin/python
import sys, os, subprocess, fnmatch, shutil, csv, re, datetime
import time
from os import listdir
from os.path import isfile, join


def checkout(buggy,patched,partone,tool,buggyfilepath,patchedfilepath):

    destdir="/Users/macbook/Documents/university/internship/D_correct-20190905T135301Z-001/D_correct"
    gendir="/Users/macbook/Documents/university/internship/D_correct-20190905T135301Z-001/gen"

    if not os.path.exists(join(destdir,"gen",tool,partone)):
        os.system("mkdir -p " + join(gendir,tool,partone,partone))

    os.system("mv " + buggy + " " + buggyfilepath + "/" + partone+"_"+partone+"_s.java")
    os.system("mv " + patched + " " + patchedfilepath + "/" + partone+"_"+partone+"_t.java")

    os.system("cp " + buggyfilepath + "/" + partone+"_"+partone+"_s.java" + " " + join(gendir, tool, partone, partone))
    os.system("cp " + patchedfilepath + "/" + partone+"_"+partone+"_t.java" + " " + join(gendir, tool, partone, partone))


if __name__ == '__main__':
    dir = '/Users/macbook/Documents/university/internship/D_correct-20190905T135301Z-001/D_correct'
    listdirs = os.listdir(dir)
    pattern = 'patch*'
    r = re.compile("([a-zA-Z]+)([0-9]+)")
    buggy=""
    patched=""

    for f in listdirs:
        if fnmatch.fnmatch(f, pattern):
                    partone = f.split("-")[1] + f.split("-")[2]
                    tool = (f.split("-")[1] + f.split("-")[3])
                    buggyfilepath=os.path.join(os.path.join(dir, f),"buggy")
                    patchedfilepath = os.path.join(os.path.join(dir, f), "patched")

                    listdirsB = os.listdir(buggyfilepath)
                    listdirsP = os.listdir(patchedfilepath)
                    for fB in listdirsB:
                        if fnmatch.fnmatch(fB, '*java'):
                            buggy=os.path.join(buggyfilepath,fB)
                            # print(buggy)

                    for fP in listdirsP:
                        if fnmatch.fnmatch(fP, '*java'):
                            patched = os.path.join(patchedfilepath, fP)
                            # print(patched)
                    if(buggy and patched):
                        checkout(buggy,patched,partone,tool,buggyfilepath,patchedfilepath)

