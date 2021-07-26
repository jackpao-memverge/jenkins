export_import_tests = [
    "redis_deep_snapshot_test_with_export",
    "run_export_import_kdb_functional_test_remote"
]

failure_job_list = []
success_job_list = []

slack_notification = '''
curl -X POST -H 'Content-type: application/json' --data \
'{"text": \
"NIGHTLY REGRESSION Export Import suite: \n \
BUILD_LOCATION: %s \n \
JOBS LIST PASSED: \n%s \n \
JOBS LIST FAILED (NIGHTLY NANNY PLEASE TRIAGE): \n%s"}' \
%s
'''

MVTEST_SUITE = "${env.JOB_BASE_NAME}"

node ("cicd_vm") {
    currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
    currentBuild.result = 'SUCCESS'
    try{
       stage ("Nightly Export Import workflow")
        {
            MVTEST_GROUP = "[Nightly] Export import test suite"
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
    finally {
            if("${SLACK_NOTIFICATION}" == 'true'){
                sh String.format(slack_notification, "${BUILD_DIR}", success_job_list, failure_job_list, "${SLACK_WEBHOOK_URL}")
            }
    }

}

def test_export_import(list){
    for (int i = 0; i < list.size(); i++) {
        echo "Test: ${list[i]}"
        b3 = build job: "test_export_import", propagate: false, parameters: [string(name: "BUILD_LABEL", value: "${MVTEST_SUITE} | ${MVTEST_GROUP} | ${list[i]}"), 
        string(name: "HOSTS_DAX_MAP", value:  "${HOSTS_DAX_MAP}"), 
        string(name: "MV_TESTS", value: "${list[i]}"), string(name: "USER_PW", value: "memverge"), 
        string(name: "NUMBER_SERVER", value: "5"), string(name: "DB_PER_SERVER", value: "5"), 
        string(name: "RECORD_PER_DB", value: "1000000"), 
        string(name: "BUILD_LOCATION", value: "${BUILD_DIR}/${RHEL_VER}/${BUILD_DATE}/${pkg_name}"), 
        string(name: "SNAPSHOT_DEPTH", value: "3"), booleanParam(name: "SKIP_NUMA_CTL", value: true), 
        string(name: "SNAPSHOT_WIDTH", value: "3"), 
        string(name: "KX_HOME", value: "/memverge/automation/KX/l64"), string(name: "MVTEST_BRANCH", value: "master"), 
        booleanParam(name: "TCMS_DRY_RUN", value: false), booleanParam(name: "TCMS_TRACE", value: true), 
        booleanParam(name: "TCMS_UPLOAD", value: true), string(name: "DEBUG_BINARIES_PATH", value: ""), 
        booleanParam(name: "SNAP_PERF_TO_DASHBOARD", value: false),
        booleanParam(name: "start_mvmallocd_service", value: true), string(name: "DramCacheGB", value: "3"), 
        booleanParam(name: "HugePageDram", value: false), booleanParam(name: "RegularDram", value: false), 
        string(name: "HOST_DAX_MAP_REMOTE", value: "${HOSTS_DAX_MAP_REMOTE}"), booleanParam(name: "runAsNoneSudo", value: false)]
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

