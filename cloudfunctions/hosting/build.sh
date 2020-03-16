#!/bin/zsh

nvm use default
root=${0:a:h}

cd ${root}/airsense

yarn install
yarn build
