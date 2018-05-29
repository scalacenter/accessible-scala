const Path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const Webpack = require('webpack');
const Merge = require("webpack-merge");

const Common = require('./webpack.common.js');
const devDir = Path.resolve(Common.rootDir, 'dev-static');



const ScalaJs = Merge(Common.ScalaJs, {
  output: {
    publicPath: '/'
  }
});

const Web = Merge(Common.Web, {
  output: {
    publicPath: '/'
  },
  entry: {
    app: Path.resolve(Common.resourcesDir, './dev.js')
  },
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: [
          "style-loader",
          "css-loader",
          "resolve-url-loader",
          "sass-loader?sourceMap"
        ]
      }
    ]
  },
  devServer: {
    hot: true,
    contentBase: [
      devDir,
      __dirname,
      Common.rootDir
    ]
  },
  plugins: [
    new Webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin({
      filename: "index.html",
      inject: "head",
      chunks: ["app"],
      template: Path.resolve(Common.resourcesDir, './index.html'),
    })
  ]
});

module.exports = Merge(
  ScalaJs,
  Web
);