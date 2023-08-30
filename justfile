alias u := upload
alias v := version
alias d := dev
alias m := master

upload:
  sh ./scripts/upload.sh

version:
  sh ./scripts/version.sh

dev:
  git checkout dev

master:
  git checkout master
