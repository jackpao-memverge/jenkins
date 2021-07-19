failure_job_list = []
slack_notification = '''
curl -X POST -H 'Content-type: application/json' --data \
'{"text": \
"NIGHTLY BULD ubuntu: \n \
"Status: %s"\n \
PIPELINE url: \n%s"}' \
%s
'''
node ("cicd_vm") {
   currentBuild.displayName = "#${BUILD_NUMBER} ${BUILD_LABEL}"
   currentBuild.result = 'SUCCESS'
   try{
       if("${SKIP_BUILD}" == 'false'){
         stage ("Compile mvcriu and mvmalloc ${BUILD_LABEL} branch")
         {
            //build job: "pipeline_nightly_build_stage", parameters: [string(name: "build_branch", value: "${BUILD_LABEL}")]

                 echo "ubuntu build"
                 build job: "pipeline_nightly_build_ubuntu", parameters: [string(name: "build_branch", value: "${BUILD_LABEL}"),
                 [$class: "LabelParameterValue", name: "node", label: "ubuntu"], string(name: "RHEL_VER", value: "${RHEL_VER}"),
                 string(name: "Pipeline_name", value: "Pipeline_master"), string(name: 'criu_branch', value: "${criu_branch}")]

         }
       }
       //stage ("Install basic workflow test")
       //{
       //  build job: 'pipeline_mvmalloc_nightly_install', parameters: [string(name: 'BUILD_LABEL', value: 'basic install test'),
       //   string(name: 'BUILD_LOCATION', value:  "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/${pkg_name}"),
       //  string(name: 'TEST_SUITE', value: 'test_fresh_install'), string(name: 'HOSTS_DAX_MAP', value: "${HOSTS_DAX_MAP}"),
       //  //string(name: 'TEST_SUITE', value: 'test_fresh_install'), string(name: 'HOSTS_DAX_MAP', value: '10.0.1.165=/dev/dax0.0,/dev/dax1.0'),
       //  string(name: 'USER_PW', value: 'memverge'), booleanParam(name: 'SKIP_NUMA_CTL', value: true), booleanParam(name: 'FORCE_CLEANUP', value: true),
       //  string(name: 'MVTEST_BRANCH', value: 'ubuntu'), booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       //}
       stage ("Functional redis test")
       {
           build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
        //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           string(name: "MV_TESTS", value: "run_redis_snapshot_functional_test"),
         //  string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
        //   string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/mvmm*x86_64.tgz"),
           string(name: "BUILD_LOCATION", value: "${BUILD_LOCATION}"),
           string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
         //  string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
           string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
           string(name: 'MVTEST_BRANCH', value: "${MVTEST_BRANCH}"),
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       }
       // stage ("Functional kdb test")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
       // }
       // stage ("Functional kdb test, run as non sudo user, mvsnapd service")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0")
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "2"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
       //     booleanParam(name: 'runAsNoneSudo', value: true),booleanParam(name: 'start_mvmallocd_service', value: true),
       //     booleanParam(name: 'mvsnapd_service', value: true)]
       // }
       stage ("Functional redis test, Hugepage cache")
       {
           build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
        //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
           string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
           string(name: "MV_TESTS", value: "run_redis_snapshot_functional_test"),
         //  string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
           string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
        //   string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/${RHEL_VER}/*/mvmm*x86_64.tgz"),
           string(name: "BUILD_LOCATION", value: "${BUILD_LOCATION}"),
           string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
         //  string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
           string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
           string(name: 'MVTEST_BRANCH', value: "${MVTEST_BRANCH}"),
           booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
           booleanParam(name: 'HugePageDram', value: true), booleanParam(name: 'RegularDram', value: false), string(name: 'DramCacheGB', value: '3')]
       }
       // stage ("Functional kdb test, regular cache")
       // {
       //     build job: "pipeline_mvmalloc_nightly_test", parameters: [string(name: "BUILD_LABEL", value: "basic redis snapshot test"),
       //  //   string(name: "HOSTS_DAX_MAP", value: "kimber.eng.memverge.com=/dev/dax0.1"),
       //     string(name: "HOSTS_DAX_MAP", value: "10.0.1.196=/dev/dax0.0,/dev/dax1.0"),
       //     string(name: "MV_TESTS", value: "run_kdb_snapshot_functional_test"),
       //  //   string(name: "USER_PW", value: "mvtest321"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "USER_PW", value: "memverge"), string(name: "NUMBER_SERVER", value: "1"),
       //     string(name: "DB_PER_SERVER", value: "1"), string(name: "RECORD_PER_DB", value: "1000000"),
       //     string(name: "BUILD_LOCATION", value: "/nfs/memverge/home/mvm/${BUILD_LABEL}_tmp/rhel8/*/${pkg_name}"),
       //     string(name: "SNAPSHOT_DEPTH", value: "1"), booleanParam(name: "SKIP_NUMA_CTL", value: true),
       //  //   string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/KX_KIMBER/l64"),
       //     string(name: "SNAPSHOT_WIDTH", value: "1"), string(name: "KX_HOME", value: "/memverge/automation/q/l64"),
       //     string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"),
       //     booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false),
       //     booleanParam(name: 'HugePageDram', value: false), booleanParam(name: 'RegularDram', value: true), string(name: 'DramCacheGB', value: '3')]
       // }
       stage ("MVMCLI basic workflow")
       {
            build job: "pipeline_mvmalloc_nightly_mvmcli", parameters: [string(name: "BUILD_LABEL", value: "mvmcli"), string(name: "BUILD_DIR",
            value: "/memverge/automation/ubuntu/${BUILD_LABEL}_tmp/${RHEL_VER}"), string(name: "BUILD_DATE", value: "*"), string(name: "TEST_SUITE", value: "basic"),
            string(name: "KX_HOME", value: "/memverge/automation/q/l64"),string(name: "HOSTS_DAX_MAP", value: "${HOSTS_DAX_MAP}"),
            string(name: "MVTEST_BRANCH", value: "${MVTEST_BRANCH}"), string(name: "BUILD_PACKAGE_NAME", value: "${pkg_name}"),
            booleanParam(name: 'TCMS_DRY_RUN', value: false), booleanParam(name: 'TCMS_TRACE', value: false)]
        }
       stage ("Package")
       {
          echo "pkg to nfs /memverge/home/mvm"
          build job: 'pipeline_mvmalloc_nightly_ubuntu_pkg', parameters: [string(name: 'RHEL_VER', value: 'ubuntu'), string(name: 'BUILD_BRANCH', value: "${BUILD_LABEL}")]
       }
   }
   catch (e){
            echo e.getMessage()
            currentBuild.result = 'FAILURE'
   }
   finally {
            emailext body:
            "*Pipline Nightly build status--master*: ${currentBuild.result}\n\n" +
            "*Pipeline run overview* ${env.JOB_URL} \n\n" +
            "*Current run url* ${env.BUILD_URL}\n\n",
            subject: "Pipeline ubuntu Nightly Build", to: 'jack.pao@memverge.com'
            if("${SLACK_NOTIFICATION}" == 'true'){
                sh String.format(slack_notification, "${currentBuild.result}", "${env.JOB_URL}", "${SLACK_WEBHOOK_URL}")

            }
   }
}
