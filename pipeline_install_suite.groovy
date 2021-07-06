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
    finally {
            if("${SLACK_NOTIFICATION}" == 'true'){
                sh String.format(slack_notification, "${BUILD_DIR}", success_job_list, failure_job_list, "${SLACK_WEBHOOK_URL}")
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
           booleanParam(name: 'FORCE_CLEANUP', value: true), string(name: 'MVTEST_BRANCH', value: "${MVTEST_BRANCH}"), 
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