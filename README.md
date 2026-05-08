# selenium-docker-test

Standalone project and scripts for performance and other docker-selenium tests

- `test-video-close`: Scripts to reproduce issue https://github.com/SeleniumHQ/docker-selenium/issues/3129
    - Run worflow `test-video-close`
    - or from the `test-video-close` folder, run `run-test.sh`
    - Note: Use linux, issue can't be reproduced in windows
- `src`: Java project to run other performance tests
- `test-grid`: Scripts & config to run performance tests on a dynamic grid
    - Measures the time to create and close the driver
    - Run workflow `test-dynamic-grid`
    - or run the individual steps in the workflow
    - Note: This can be reproduced in linux and windows
