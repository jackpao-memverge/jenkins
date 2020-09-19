//"kx_insert_performance_test" 
export_import_tests = [
    "run_export_import_kdb_deep_test_local",
    "run_export_import_kdb_deep_test_remote",
    "run_export_import_kdb_functional_test_local",
    "run_export_import_kdb_functional_test_remote",
    "run_export_import_kdb_wide_test_local",
    "run_export_import_kdb_wide_test_remote",
    "run_export_import_kdb_functional_test_remote_bg_load",
    "run_export_import_kdb_functional_test_remote_bg_kdb_load",
    "kx_deep_snapshot_test_with_export",
    "redis_deep_snapshot_test_with_export"
]
nonsudo = [ false, true ]

failure_job_list = []
success_job_list = []
node ("cicd_vm") {
    currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
    currentBuild.result = 'SUCCESS'
   try{
       stage ("Export Import")
        {
            b1_result = 'SUCCESSFUL'
            export_import(export_import_tests, nonsudo)
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
    }

}

def export_import(list, param) {
    for (int i = 0; i < list.size(); i++) {
        echo "Test: ${list[i]}"
            for (int j = 0; j < param.size(); j++) {
                echo "start app with none sudo: ${param[j]}"
                b1 =  build job: "test_export_import", parameters: [string(name: "BUILD_LABEL", value: "${BUILD_LABEL}-mmuser:${param[j]}"), 
                string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"), 
                string(name: "MV_TESTS", value: "${list[i]}"), 
                string(name: "USER_PW", value: "${USER_PW}"), string(name: "NUMBER_SERVER", value: "10"), 
                string(name: "DB_PER_SERVER", value: "5"), string(name: "RECORD_PER_DB", value: "1000000"), 
                string(name: "BUILD_LOCATION", value: "${BUILD_LOCATION}"), 
                string(name: "SNAPSHOT_DEPTH", value: "10"), booleanParam(name: "SKIP_NUMA_CTL", value: true), 
                string(name: "SNAPSHOT_WIDTH", value: "10"), string(name: "KX_HOME", value: "${KX_HOME}"), 
                string(name: "MVTEST_BRANCH", value: "master"), booleanParam(name: "TCMS_DRY_RUN", value: false), 
                booleanParam(name: "TCMS_TRACE", value: true), booleanParam(name: "TCMS_UPLOAD", value: true), 
                string(name: "DEBUG_BINARIES_PATH", value: ""), booleanParam(name: "SNAP_PERF_TO_DASHBOARD", value: false), 
                string(name: "CPU_NODE_BIND", value: "1"), booleanParam(name: "start_mvmallocd_service", value: true), 
                string(name: "DramCacheGB", value: "3"), booleanParam(name: "HugePageDram", value: false),
                booleanParam(name: "RegularDram", value: false), string(name: "HOST_DAX_MAP_REMOTE", value: "${HOST_DAX_MAP_REMOTE}"), 
                booleanParam(name: "runAsNoneSudo", value: "${param[j]}")]      
                try{
                        if(b1.result == 'FAILURE'|| b1.result == 'ABORTED') {
                            echo "${list[i]} job failed"
                            failure_job_list.add("${list[i]}: ${b1.absoluteUrl} \n")
                            b1_result = 'FAILURE'
                            sh "echo ${list[i]} job failed; exit 1"
                        }
                        else{
                            success_job_list.add("${list[i]}, sudorer:${param[j]}: ${b1.absoluteUrl} \n")
                        }
                    }
                    catch (e){
                        echo e.getMessage()
                    }
    
            }
    
    }
}