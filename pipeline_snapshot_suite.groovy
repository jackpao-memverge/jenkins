mvmcli_tests = ["all"]

//begin removing kdb tests
//"kx_insert_performance_test" 
snapshot_tests = ["redis_deep_snapshot_test","redis_wide_snapshot_test",
"run_redis_snapshot_functional_test","snapshot_clone_test",
"snapshot_with_load_in_bg_test"]

snapshot_tests_mvsnapd = ["redis_deep_snapshot_test","redis_wide_snapshot_test",
"snapshot_with_load_in_bg_test"]


failure_job_list = []
success_job_list = []

slack_notification = '''
curl -X POST -H 'Content-type: application/json' --data \
'{"text": \
"NIGHTLY REGRESSION ubuntu: \n \
BUILD_LOCATION: %s \n \
JOBS LIST PASSED: \n%s \n \
JOBS LIST FAILED (NIGHTLY NANNY PLEASE TRIAGE): \n%s"}' \
%s
'''

node ("cicd_vm") {
    currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
    currentBuild.result = 'SUCCESS'
   try{
       stage ("Nightly Snapshot test suite")
        {
            b1_result = 'SUCCESSFUL'
            snapshot_suite_all(snapshot_tests)
            if(b1_result == 'FAILURE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }
   try{
       stage ("Nightly Snapshot test suite, Hugepage cache 5G")
        {
            b1_result = 'SUCCESSFUL'
            snapshot_suite_all_huge(snapshot_tests)
            if(b1_result == 'FAILURE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }
   try{
       stage ("Nightly Snapshot test suite, regular cache 5G")
        {
            b1_result = 'SUCCESSFUL'
            snapshot_suite_all_regular(snapshot_tests)
            if(b1_result == 'FAILURE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }
    try{
       stage ("Nightly Snapshot test suite, mvsnapd service")
        {
            b1_result = 'SUCCESSFUL'
            snapshot_suite_all_mvsnapd(snapshot_tests_mvsnapd)
            if(b1_result == 'FAILURE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }
    try{
       stage ("Nightly Snapshot test suite, mm bin installation skip for now")
        {
            b1_result = 'SUCCESSFUL'
            //snapshot_suite_all_mm_bin(snapshot_tests)
            if(b1_result == 'FAILURE') {
                echo "Stage failed"
                sh "echo Stage failed;exit 1"
            }
         }
    }
    catch (e){
        echo e.getMessage()
        currentBuild.result = 'UNSTABLE'
    }

    finally {
            if("${SLACK_NOTIFICATION}" == 'true'){
                sh String.format(slack_notification, "${BUILD_DIR}", success_job_list, failure_job_list, "${SLACK_WEBHOOK_URL}")
            }
    }

}

def snapshot_suite_all(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           if ("${list[i]}" == "snapshot_clone_test"){
               skip_numa = false
               kx_home = "/memverge/automation/KX/l64"
               num_server = 5
               db_per_server = 1
               //node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               //node_worker = "jack_worker"
           }
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "${num_server}"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "${db_per_server}"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value:"${skip_numa}"),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${kx_home}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           //[$class: "LabelParameterValue", name: "node", label: "${node_worker}"]
           ]
           try{
               if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}_Pmemonly: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}

def snapshot_suite_all_huge(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           if ("${list[i]}" == "snapshot_clone_test"){
               skip_numa = false
               kx_home = "/memverge/automation/KX/l64"
               num_server = 5
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }           
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "${num_server}"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "${db_per_server}"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value:"${skip_numa}"),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${kx_home}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'HugePageDram', value: true), booleanParam(name: 'RegularDram', value: false), string(name: 'DramCacheGB', value: '5'),
           booleanParam(name: 'start_mvmallocd_service', value: true),
           //[$class: "LabelParameterValue", name: "node", label: "${node_worker}"]
           ]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}_HugePage: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}_HugePage: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}

def snapshot_suite_all_regular(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           if ("${list[i]}" == "snapshot_clone_test"){
               skip_numa = false
               kx_home = "/memverge/automation/KX/l64"
               num_server = 5
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }           
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "${num_server}"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "${db_per_server}"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value: "${skip_numa}"),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${kx_home}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'HugePageDram', value: false), booleanParam(name: 'RegularDram', value: true), string(name: 'DramCacheGB', value: '5'),
           //[$class: "LabelParameterValue", name: "node", label: "${node_worker}"]
           ]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}_Dram: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}_Dram: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}

def snapshot_suite_all_mvsnapd(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           if ("${list[i]}" == "snapshot_clone_test"){
               skip_numa = false
               kx_home = "/memverge/automation/KX/l64"
               num_server = 5
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression mvsnapd ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "${num_server}"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "${db_per_server}"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value:"${skip_numa}"),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${kx_home}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'start_mvmallocd_service', value: true),
           booleanParam(name: 'mvsnapd_service', value: true),
           //[$class: "LabelParameterValue", name: "node", label: "${node_worker}"]
           ]
           try{
               if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}_Mvsnapd: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}

def snapshot_suite_all_mm_bin(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           if ("${list[i]}" == "snapshot_clone_test"){
               skip_numa = false
               kx_home = "/memverge/automation/KX/l64"
               num_server = 10
               db_per_server = 1
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
           }           
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value:  "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "${num_server}"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "${db_per_server}"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${kx_home}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           string(name: "MM_INSTALL_BIN", value: "${mm_install_path}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'HugePageDram', value: false), booleanParam(name: 'RegularDram', value: true), string(name: 'DramCacheGB', value: '5')]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}_Dram: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}_MMbin: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}
