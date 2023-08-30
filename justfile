alias u := upload
alias v := version
alias d := dev
alias m := master

init:
  mkdir -p ./.git/hooks/
  printf "#!/bin/sh\nsh ./scripts/test.sh\n" > ./.git/hooks/pre-commit

upload:
  sh ./scripts/upload.sh

version:
  sh ./scripts/version.sh

dev:
  git checkout dev

master:
  git checkout master
