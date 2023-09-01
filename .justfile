alias t := test
alias u := upload
alias v := version
alias d := dev
alias m := merge
alias ma := master
alias i := install
alias o := open

package-name := "com.andrew.timetable"

scripts_dir := "scripts"
test_script := "test.sh"

hooks_dir := ".git/hooks"
pre_commit_file := hooks_dir + "/pre-commit"
pre_commit_file_content := "\
#!/bin/sh
sh ./" + scripts_dir + "/" + test_script + "
"

init:
  if ! test -f "{{pre_commit_file}}"; then \
    mkdir -p "{{hooks_dir}}"; \
    printf "{{pre_commit_file_content}}" > "{{pre_commit_file}}"; \
    chmod +x "{{pre_commit_file}}"; \
  fi

test:
  sh ./scripts/test.sh

upload: init
  sh ./scripts/upload.sh

version:
  sh ./scripts/version.sh

dev:
  git checkout dev

master:
  git checkout master

merge branch="dev":
  git merge --no-ff {{branch}}

install:
  adb install ./app/build/outputs/apk/release/Timetable_v*.apk

open:
  adb shell am start -n {{package-name}}/{{package-name}}.MainActivity
