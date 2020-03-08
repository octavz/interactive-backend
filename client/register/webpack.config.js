const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const resDir= path.resolve(__dirname + '../../../src/main/resources/client');
const outputDir = path.join(resDir, 'register/');

const isProd = process.env.NODE_ENV === 'production';

module.exports = {
  entry: './src/Index.bs.js',
  mode: isProd ? 'production' : 'development',
  output: {
    path: outputDir,
    filename: 'index.js'
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: 'src/index.html',
      inject: true,
      hash:true
    })
  ],
  devServer: {
    compress: true,
    contentBase: outputDir,
    port: process.env.PORT || 8000,
    historyApiFallback: true
  }
};

