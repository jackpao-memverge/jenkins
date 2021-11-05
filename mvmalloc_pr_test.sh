#!/bin/bash
env
set -x

echo "target branch: ${ghprbTargetBranch}"

#checkout the target branch of criu 
if [[ ${ghprbTargetBranch} == "master" ]]; then
	criu_branch="mvcriu"
else
	criu_branch="${ghprbTargetBranch}"
fi

git clone --single-branch --branch $criu_branch https://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@github.com/MemVerge/criu ${WORKSPACE}/mvcriu


DAX_UUID=("04d40640-c069-4ad5-b974-fc0c9070f65c")

DAX="/dev/"
for uuid in ${DAX_UUID[*]}; do
  dax_num=$(ndctl list | jq -r '.[] | select(.uuid=='\"${uuid}\"') | .chardev')
  if [[ "$dax_num" != "" ]]; then
    DAX="${DAX}${dax_num}"
    break
  fi
done

if [[ "$DAX" == "/dev/" ]]; then                                                                                                                                                
  echo "Dax device UUID(s) ${DAX_UUID[*]} not exist, use dax0.0 from vm build pool"                                                                                                                
  DAX="/dev/dax0.0"                                                                                                                                                             
else                                                                                                                                                                            
  echo "Using dax device $DAX"                                                                                                                                                  
fi                         

echo "Clean up first, make sure existing pull request mvmallocd process is not running"

lsof | grep $DAX | grep -v grep | awk '{{print $2}}' | sort -nu | xargs kill || true

sleep 2

lsof | grep $DAX | grep -v grep | awk '{{print $2}}' | sort -nu | xargs kill -9 || true


echo "Build mvcriu to generate mvsnap and dome.so"
cp build_script/build/build_MemVerge_mvcriu.sh ${WORKSPACE}

./build_MemVerge_mvcriu.sh rhel7

echo "Copy mvsnap and dpme.so to workspace"
mkdir ${WORKSPACE}/mvsnap_dir
cp mvcriu/mvsnap ${WORKSPACE}/mvsnap_dir
cp mvcriu/plugin/dpme.so ${WORKSPACE}/mvsnap_dir


echo "Mvmalloc Pull Request build test"

cd ${WORKSPACE}

sh ci/build-pr.sh || exit 1


OS_VER=$(grep '^VERSION_ID' /etc/os-release | sed 's/"//g'| cut -d "=" -f2)

if [[ $OS_VER == 7 ]]; then
  source scl_source enable devtoolset-8
fi

git submodule update --init --recursive --depth 1

make clean

make DEBUG=0 STATIC_LIBPMEM=1

