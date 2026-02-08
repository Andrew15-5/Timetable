#!/bin/sh
cd "$(dirname "$0")" || exit 1
scripts_dir=$(basename "$PWD")
cd .. # Exit scripts/ dir

version_pattern='(\d\.){2}\d'
version_lines_pattern='^[-+].*version(Code|Name)'

open() {
  (nohup "$@" &) > /dev/null 2>&1
}

# $1 - version to verify
is_valid_version() {
  echo "$1" | grep -qPx "$version_pattern"
}

get_only_version_tags() {
  grep -Px "v$version_pattern"
}

get_latest_version_tag() {
  sort -Vr | head -n 1
}

get_version_tag_of_last_commit() {
  git tag --contains HEAD | get_only_version_tags | get_latest_version_tag
}

get_last_version_tag() {
  git tag --list | get_only_version_tags | get_latest_version_tag
}

unstaged_version_change_exists() {
  git diff app/build.gradle | grep -qE "$version_lines_pattern"
}

staged_version_change_exists() {
  git diff --cached app/build.gradle | grep -qE "$version_lines_pattern"
}

stage_new_version() {
  echo 'Add updated version lines to stage? (after confirmation)'
  printf 'Press Enter key to proceed (Ctrl+C to cancel)'
  read -r _
  git add -p app/build.gradle
}

commit_new_version() {
  echo 'Including version updated in the last commit'
  git commit --amend --no-edit
}

update_version() {
  echo 'Checking version'
  if unstaged_version_change_exists; then
    echo 'Found unstaged version'
    stage_new_version
    commit_new_version
  elif staged_version_change_exists; then
    echo 'Found staged version'
    commit_new_version
  fi
  version_tag=$(get_version_tag_of_last_commit)
  if [ -z "$version_tag" ]; then
    echo "Last commit doesn't have version tag"
    version_from_file="$(sh "./$scripts_dir/version.sh")"
    if [ "v$version_from_file" = "$(get_last_version_tag)" ]; then
      printf 'Provide new version (current version: %s): ' "$version_from_file"
      read -r new_version
      is_valid_version "$new_version"
      sh "./$scripts_dir/version.sh" "$new_version"
      stage_new_version
      commit_new_version
    fi
    version=$(sh "./$scripts_dir/version.sh")
    version_tag="v$version"
    echo "Adding $version_tag tag"
    git tag "$version_tag"
  fi
  echo 'Pushing commits'
  git push
  echo 'Pushing tags'
  git push --tags
}

build_release() {
  echo 'Building release APK file'
  ./gradlew --warning-mode all :app:assembleRelease :app:lintAnalyzeDebug
}

(set -e
  update_version

  build_release
  dir='app/build/outputs/apk/release'
  apk="$dir/app-release.apk"
  metadata="$dir/output-metadata.json"
  # If file exits and has last version
  if ! {
    [ -f "$apk" ] && grep -qF '"versionName": "'"$version"'"' "$metadata"
  }; then
    >&2 echo "APK file wasn't built"
    exit 1
  fi

  if : "${BROWSER:?}"; then
    set -- "$BROWSER" '--new-window'
  else
    set -- 'xdg-open'
  fi

  page="https://github.com/Andrew15-5/timetable/releases/new"
  arguments="tag=$version_tag&title=$version_tag"
  url="$page?$arguments"
  echo 'Opening GitHub page for new version'
  open "$1" "$2" "$url"

  echo 'Renaming APK file'
  mv "$apk" "$dir/Timetable_$version_tag.apk"

  sleep 2 # Wait for browser page to open

  echo 'Opening directory from which you can Drag and Drop APK file to browser'
  open xdg-open "$dir"
)
