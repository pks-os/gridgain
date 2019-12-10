/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 * 
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {WebSocketHook} from '../../mocks/WebSocketHook';
import {agentStat, AGENT_DISCONNECTED_GRID} from '../../mocks/agentTasks';
import {resolveUrl, dropTestDB, insertTestUser, enableDemoMode} from '../../environment/envtools';
import {prepareUser, cleanupUser} from '../../roles';
import {errorNotification} from '../../components/notifications';

fixture('Notebook')
    .beforeEach(async(t) => {
        await t.addRequestHooks(
            t.ctx.ws = new WebSocketHook()
                .use(agentStat(AGENT_DISCONNECTED_GRID))
        );
        await prepareUser(t);
        await enableDemoMode();
    })
    .afterEach(async(t) => {
        t.ctx.ws.destroy();
        await cleanupUser(t);
    });

test('Opening a notebook with disconnected agent', async(t) => {
    await t.navigateTo(resolveUrl('/notebook/demo'));
    await t.expect(errorNotification.withText('Failed to find connected agent for this account').exists).notOk('No errors on opening of page when agent is disconnected.');
});
