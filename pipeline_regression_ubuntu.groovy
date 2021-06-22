mvmcli_tests = ["all"]

//begin removing kdb tests
//"kx_insert_performance_test" 
snapshot_tests = ["redis_deep_snapshot_test","redis_wide_snapshot_test",
"run_redis_snapshot_functional_test","snapshot_clone_test",
"snapshot_with_load_in_bg_test"]

snapshot_tests_mvsnapd = ["redis_deep_snapshot_test","redis_wide_snapshot_test",
"snapshot_with_load_in_bg_test"]

export_import_tests = [
    "redis_deep_snapshot_test_with_export",
    "run_export_import_kdb_functional_test_remote"
]

//mvmcli_tests = []
//end removing kdb tests, more below

install_tests = ["test_fresh_install","test_fresh_uninstall"]

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
       stage ("Nightly Install test suite")
        {
            b0_result = 'SUCCESSFUL'
            install_suite_all(install_tests)
            if(b0_result == 'FAILURE') {
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
       stage ("Nightly Snapshot test suite, mm bin installation")
        {
            b1_result = 'SUCCESSFUL'
            snapshot_suite_all_mm_bin(snapshot_tests)
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
         stage ("Nightly MVMCLI workflow")
          {
              b2_result = 'SUCCESSFUL'
              mvmcli_suite_all(mvmcli_tests)
              if(b2_result == 'FAILURE') {
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
       stage ("Nightly Export Import workflow")
        {
            b3_result = 'SUCCESSFUL'
            test_export_import(export_import_tests)
            if(b3_result == 'FAILURE') {
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
       stage ("Nightly hazelcastcluster workflow")
        {
            b4_result = 'SUCCESSFUL'
            hazelcastcluster()
            if(b4_result == 'FAILURE') {
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
               num_server = 10
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }
           b1 = build job: "ubuntu-pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
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
           [$class: "LabelParameterValue", name: "node", label: "${node_worker}"]]
           try{
               if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
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
               num_server = 10
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }           
           b1 = build job: "ubuntu-pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
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
           [$class: "LabelParameterValue", name: "node", label: "${node_worker}"]]
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
               num_server = 10
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }           
           b1 = build job: "ubuntu-pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
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
           [$class: "LabelParameterValue", name: "node", label: "${node_worker}"]]
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
               num_server = 10
               db_per_server = 1
               node_worker = "ubuntu"
           }else{
               skip_numa = true
               kx_home = "/memverge/automation/KX_MISSIONB/l64"
               num_server = 5
               db_per_server = 10
               node_worker = "jack_worker"
           }
           b1 = build job: "ubuntu-pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression mvsnapd ${list[i]}"),
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
           [$class: "LabelParameterValue", name: "node", label: "${node_worker}"]]
           try{
               if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                //if(b1.result == 'FAILURE') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                    b1_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}


def mvmcli_suite_all(list){
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           b2 = build job: "pipeline_mvmalloc_nightly_mvmcli", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"), 
           string(name: "BUILD_DIR", value: "${BUILD_DIR}/${RHEL_VER}"), string(name: "BUILD_DATE", value: "${BUILD_DATE}"), 
           string(name: "TEST_SUITE", value: "${list[i]}"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"), 
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"), string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"), 
           string(name: "BUILD_PACKAGE_NAME", value: "${pkg_name}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'Exclude_Host_Reboot', value: true)]
           try{
                if(b2.result == 'FAILURE'|| b2.result == 'ABORTED') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b2.absoluteUrl} \n")
                    b2_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}: ${b2.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
        
    }    
}
def install_suite_all(list){
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           b0 = build job: 'pipeline_mvmalloc_nightly_install', propagate: false, parameters: [string(name: 'BUILD_LABEL', value: 'Nightly regression'), 
           string(name: 'BUILD_LOCATION', value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"), 
           string(name: 'TEST_SUITE', value: "${list[i]}"), string(name: 'HOSTS_DAX_MAP', value:  "${HOSTS_DAX_MAP}"), 
           string(name: 'USER_PW', value: 'memverge'), booleanParam(name: 'SKIP_NUMA_CTL', value: true), 
           booleanParam(name: 'FORCE_CLEANUP', value: true), string(name: 'MVTEST_BRANCH', value: 'ubuntu'), 
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
            try{
                if(b0.result == 'FAILURE'|| b0.result == 'ABORTED') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b0.absoluteUrl} \n")
                    b0_result = 'FAILURE'
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

def test_export_import(list){
    for (int i = 0; i < list.size(); i++) {
        echo "Test: ${list[i]}"
        b3 = build job: "test_export_import", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"), 
        string(name: "HOSTS_DAX_MAP", value:  "${HOSTS_DAX_MAP}"), 
        string(name: "MV_TESTS", value: "${list[i]}"), string(name: "USER_PW", value: "memverge"), 
        string(name: "NUMBER_SERVER", value: "5"), string(name: "DB_PER_SERVER", value: "5"), 
        string(name: "RECORD_PER_DB", value: "1000000"), 
        string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"), 
        string(name: "SNAPSHOT_DEPTH", value: "7"), booleanParam(name: "SKIP_NUMA_CTL", value: true), 
        string(name: "SNAPSHOT_WIDTH", value: "7"), 
        string(name: "KX_HOME", value: "/memverge/automation/KX/l64"), string(name: "MVTEST_BRANCH", value: "master"), 
        booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: true), 
        booleanParam(name: "TCMS_UPLOAD", value: true), string(name: "DEBUG_BINARIES_PATH", value: ""), 
        booleanParam(name: "SNAP_PERF_TO_DASHBOARD", value: false),
        booleanParam(name: "start_mvmallocd_service", value: true), string(name: "DramCacheGB", value: "3"), 
        booleanParam(name: "HugePageDram", value: false), booleanParam(name: "RegularDram", value: false), 
        string(name: "HOST_DAX_MAP_REMOTE", value: "10.0.1.88=/dev/dax0.0,/dev/dax1.0"), booleanParam(name: "runAsNoneSudo", value: false)]
        try{
                if(b3.result == 'FAILURE'|| b3.result == 'ABORTED') {
                    echo "${list[i]} job failed"
                    failure_job_list.add("${list[i]}: ${b3.absoluteUrl} \n")
                    b3_result = 'FAILURE'
                    sh "echo ${list[i]} job failed; exit 1"
                }
                else{
                    success_job_list.add("${list[i]}: ${b3.absoluteUrl} \n")
                }
            }
        catch (e){
                echo e.getMessage()
        }
    }

}

def hazelcastcluster(){
    b4 = build job: 'hazelcastcluster', parameters: [string(name: 'BUILD_LABEL', value: 'snapshot test'),
    string(name: 'TEST_SUITE', value: 'clusterPidns'), string(name: 'HOSTS_DAX_MAP', value:  "${HOSTS_DAX_MAP}"),
    string(name: 'BUILD_LOCATION', value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"),
    string(name: 'NUM_MEMBERS', value: ''), string(name: 'NUM_WORKERS', value: ''), string(name: 'NUM_RECORDS', value: ''),
    string(name: 'STEP_SIZE', value: ''), string(name: 'USER_PW', value: 'memverge'), 
    string(name: 'MM_PYTHON_BIN', value: '/memverge/automation/anaconda3/bin/python3'), string(name: 'JUPYTER_BIN', value: '/memverge/automation/anaconda3/bin'),
    booleanParam(name: 'SKIP_REBOOT_WORKFLOW', value: false), string(name: 'EXTRA_PARAMS', value: ''),
    string(name: 'BOT_TEST_NAME', value: ''), string(name: 'RESULT_PATH', value: ''), string(name: 'PUT_IN_INFLUXDB', value: ''),
    string(name: 'DEBUG_BINARIES_PATH', value: ''),
    string(name: 'LOG_LVL', value: '2')]
    try{
                if(b4.result == 'FAILURE'|| b4.result == 'ABORTED') {
                    echo "hazelcast_test_clusterPidns job failed"
                    failure_job_list.add("hazelcast_test_clusterPidns: ${b4.absoluteUrl} \n")
                    b4_result = 'FAILURE'
                    sh "echo hazelcast_test_clusterPidns job failed; exit 1"
                }
                else{
                    success_job_list.add("hazelcast_test_clusterPidns: ${b4.absoluteUrl} \n")
                }
            }
        catch (e){
                echo e.getMessage()
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
           b1 = build job: "ubuntu-pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
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
                    success_job_list.add("${list[i]}_Dram: ${b1.absoluteUrl} \n")
                }
            }
            catch (e){
                echo e.getMessage()
            }
    }
}
