//
// Copyright (C) 2025 Square, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
let fs = require('node:fs');
let path = require('node:path');

function installSnapshotsStore(config, fullyQualifiedProjectDirectory) {
  function isSnapshotRequest(method, urlPath) {
    if (method === 'GET' || method === 'POST') {
      return urlPath.startsWith('/dom-tester-snapshots/')
        || urlPath.startsWith('/build/dom-tester-snapshots/');
    } else {
      return false;
    }
  }

  function SnapshotStoreMiddlewareFactory(config) {
    return function (request, response, next) {
      let url = new URL(request.originalUrl, "https://example.com/");
      let urlPath = url.pathname;

      if (!isSnapshotRequest(request.method, urlPath)) {
        return next();
      }

      let filePath = path.join(fullyQualifiedProjectDirectory, urlPath);

      if (!filePath.startsWith(`${fullyQualifiedProjectDirectory}/`)) {
        return next(); // Directory traversal attack? Don't touch the file system.
      }

      if (request.method === 'GET') {
        fs.readFile(filePath, (err, data) => {
          if (err) {
            response.writeHead(404);
            response.end('no such file');
          } else {
            response.end(data);
          }
        });
      } else {
        try {
          fs.mkdirSync(path.dirname(filePath), {recursive: true});
        } catch (err) {
          // Already exists?
        }

        let writeStream = fs.createWriteStream(filePath);
        request.on('data', function (chunk) {
          writeStream.write(chunk);
        });

        request.on('end', function () {
          writeStream.end();
          response.end('accepted');
        });
      }
    }
  }

  config.plugins = config.plugins || [];
  config.plugins.push({'middleware:snapshot-store': ['factory', SnapshotStoreMiddlewareFactory]});

  config.middleware = config.middleware || [];
  config.middleware.push('snapshot-store');
}

/**
 * Karma runs a web server that hosts test code. Mocha is a JavaScript test framework. We configure
 * timeouts in Karma's config under client.mocha.timeout.
 */
function configureMochaTimeout(config, timeout) {
  config.client = config.client || {};
  config.client.mocha = config.client.mocha || {};
  config.client.mocha.timeout = timeout;
}
