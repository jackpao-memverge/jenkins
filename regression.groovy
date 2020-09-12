//"kx_insert_performance_test" 
//snapshot_tests = ["redis_deep_snapshot_test","redis_wide_snapshot_test","kx_deep_snapshot_test","kx_wide_snapshot_test","run_redis_snapshot_functional_test","run_kdb_snapshot_functional_test","snapshot_with_load_in_bg_test","mixed_apps_test"]
snapshot_tests = ["run_redis_snapshot_functional_test","run_kdb_snapshot_functional_test"]
mvmcli_tests = ["mvmcliBasicWorkFlows"]
install_tests = ["run_all"]
failure_job_list = []
success_job_list = []
node ("cicd_vm") {
    currentBuild.displayName = "#${BUILD_NUMBER} ${OS_VER} ${HOSTS_DAX_MAP}"
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
    finally {
            emailext body:
            "*Pipline status*: ${currentBuild.result}\n\n" +
            "*Pipeline run overview* ${env.JOB_URL} \n\n" +
            "*Current run url* ${env.BUILD_URL}\n\n" +
            "*Successful job list*\n${success_job_list}\n\n" +
            "*Failed job list*\n${failure_job_list}\n\n",
            subject: "Pipeline Compatibility test", to: 'jack.pao@memverge.com'
    }

}

def snapshot_suite_all(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "${USER_PW}"), string(name: "NUMBER_SERVER", value: "5"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "10"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/mvmm*x86_64.tgz"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${KX_HOME}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'start_mvmallocd_service', value: true)]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
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
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "${USER_PW}"), string(name: "NUMBER_SERVER", value: "5"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "10"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/mvmm*x86_64.tgz"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${KX_HOME}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'HugePageDram', value: true), booleanParam(name: 'RegularDram', value: false), string(name: 'DramCacheGB', value: '5'),
           booleanParam(name: 'start_mvmallocd_service', value: true)]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
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

def snapshot_suite_all_regular(list) {
    for (int i = 0; i < list.size(); i++) {
           echo "Test: ${list[i]}"
           b1 = build job: "pipeline_mvmalloc_nightly_test", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "Nightly regression ${list[i]}"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           //string(name: "HOSTS_DAX_MAP", value: "padre-d.eng.memverge.com=/dev/dax0.0,/dev/dax1.0"),
           string(name: "MV_TESTS", value: "${list[i]}"),
           string(name: "USER_PW", value: "${USER_PW}"), string(name: "NUMBER_SERVER", value: "5"),
           //string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "5"),
           string(name: "DB_PER_SERVER", value: "10"), string(name: "RECORD_PER_DB", value: "1000000"),
           string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/mvmm*x86_64.tgz"),
           //string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/rhel8/${BUILD_DATE}/mvmm*x86_64.tgz"),
           string(name: "SNAPSHOT_DEPTH", value: "5"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
           string(name: "SNAPSHOT_WIDTH", value: "5"), string(name: "KX_HOME", value: "${KX_HOME}"),
           string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'HugePageDram', value: false), booleanParam(name: 'RegularDram', value: true), string(name: 'DramCacheGB', value: '5'),
           booleanParam(name: 'start_mvmallocd_service', value: true)]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
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
           string(name: "TEST_SUITE", value: "${list[i]}"), string(name: "KX_HOME", value: "${KX_HOME}"), 
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"), string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"), 
           string(name: "BUILD_PACKAGE_NAME", value: "mvmm*x86_64.tgz"),
           booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: false),
           booleanParam(name: 'Exclude_Host_Reboot', value: true)]
           try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
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
           b0 = build job: 'pipeline_mvmalloc_nightly_install', parameters: [string(name: 'BUILD_LABEL', value: 'Nightly regression'), 
           string(name: 'BUILD_LOCATION', value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/mvmm*x86_64.tgz"), 
           string(name: 'TEST_SUITE', value: "${list[i]}"), string(name: 'HOSTS_DAX_MAP', value: "${HOSTS_DAX_MAP}"), 
           string(name: 'USER_PW', value: 'memverge'), booleanParam(name: 'SKIP_NUMA_CTL', value: true), 
           booleanParam(name: 'FORCE_CLEANUP', value: true), string(name: 'MVTEST_BRANCH', value: 'master'), 
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
            try{
                if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
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
