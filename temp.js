host_list = ["10.0.1.197=/dev/dax0.0,/dev/dax1.0"]
failure_job_list = []
success_job_list = []
node ("cicd_vm") {
    currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
    currentBuild.result = 'SUCCESS'
   try{
       stage ("Start compatibility test")
        {
            b0_result = 'SUCCESSFUL'
            comp_suite_all(install_tests)
            if(b0_result == 'UNSTABLE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }
}

def comp_suite_all(list){
    for (int i = 0; i < list.size(); i++) {
           echo "Host: ${list[i]}"
           b0 = build job: 'Pipeline_compatibility', parameters: [string(name: 'BUILD_LABEL', value: 'OS Compat'), 
           string(name: 'OS_VER', value: 'centos78_420'), string(name: 'HOSTS_DAX_MAP', value: "$list[i]"), 
           string(name: 'MVTEST_BRANCH', value: 'master'), string(name: 'BUILD_DIR', value: '/memverge/home/mvm/release/0.9'), 
           string(name: 'BUILD_DATE', value: 'latest')]
           try{
                if(b0.result == 'UNSTABLE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b0.absoluteUrl} \n")
                    b0_result = 'UNSTABLE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}: ${b0.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }    
}
