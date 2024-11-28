import React, { Component } from 'react'
import { Select, Button } from 'antd'
import { filterAction } from '../util/filterProgress'
import { message } from 'antd'
import jsonDiff from 'fast-json-patch'

const { Option } = Select

export default class ProgressFilter extends Component {
  constructor() {
    super()
    this.state = {
      selectValue: [],
    }
  }

  filterProgress = value => {
    let { minder } = this.props
    let recordId = this.props.wsParam.query.recordId
    let sessionKey = 'originminder_' + recordId
    let sessionValue = sessionStorage.getItem(sessionKey)
    if (value.includes(100) || value.length === 0) {
      if (sessionValue != null) {
        minder._status = 'readonly'
        minder.importJson(JSON.parse(sessionValue))
        minder._status = 'normal'
      }
      return
    } else {
      let caseContentJson = minder.exportJson()
      if (sessionValue != null) {
        caseContentJson = JSON.parse(sessionValue)
      } else {
        sessionStorage.setItem(sessionKey, JSON.stringify(caseContentJson))
      }
      let filterProgresses = value
      let result = filterAction(caseContentJson, filterProgresses)
      if (result == null) {
        //提示过滤后为空
        message.warn('过滤后内容为空')
      } else {
        minder._status = 'readonly'
        minder.importJson(result)
        minder._status = 'normal'
      }
    }
  }

  componentWillUnmount() {
    let recordId = this.props.wsParam.query.recordId
    let sessionKey = 'originminder_' + recordId
    let sessionValue = sessionStorage.getItem(sessionKey)
    if (sessionValue != null) {
      sessionStorage.removeItem(sessionKey)
    }
  }

  handleChange = value => {
    if (value.length > 1 && value.includes(100)) {
      this.props.changeProgressFilterValue([100])
    } else {
      this.props.changeProgressFilterValue(value)
    }
  }

  // jsonPatch = () => {
  //   let origin = {
  //     root: {
  //       data: {
  //         id: 'cr06if69bhk0',
  //         created: 1562059643204,
  //         text: '我的用例哦',
  //         create: 1678195094233,
  //       },
  //       children: [
  //         { data: { id: 'cr06igg72r40', created: 1678195097011, text: '我的用例' }, children: [] },
  //       ],
  //     },
  //     template: 'right',
  //     theme: 'classic-compact',
  //     version: '1.4.43',
  //     base: 3,
  //     right: 1,
  //   }
  //   let patch = [
  //     {
  //       op: 'add',
  //       path: '/root/children/0/children/0',
  //       value: {
  //         data: {
  //           id: 'cr06q161yg00',
  //           created: 1678195690659,
  //           text: 'hao de ',
  //         },
  //         children: [],
  //       },
  //     },
  //     {
  //       op: 'add',
  //       path: '/root/children/0/children/1',
  //       value: {
  //         data: {
  //           id: 'cr06q162mmg0',
  //           created: 1678195690660,
  //           text: 'liaojie ',
  //         },
  //         children: [],
  //       },
  //     },
  //     {
  //       op: 'add',
  //       path: '/root/children/0/children/2',
  //       value: {
  //         data: {
  //           id: 'cr06q1634680',
  //           created: 1678195690661,
  //           text: 'zhidao',
  //         },
  //         children: [],
  //       },
  //     },
  //   ]

  //   let newContnet = jsonDiff.applyPatch(origin, patch).newDocument
  //   console.log('newContnet====', newContnet)
  // }
  render() {
    return (
      <div>
        <Select
          mode="multiple"
          style={{ width: '200px' }}
          placeholder="筛选用例"
          value={this.props.progressFilterValue}
          // allowClear
          maxTagCount={3}
          onChange={value => this.handleChange(value)}
          optionLabelProp="label"
          onBlur={value => {
            this.filterProgress(value)
          }}
        >
          <Option value={100} label="全部">
            全部
          </Option>
          <Option value={99} label="未执行">
            未执行
          </Option>
          <Option value={9} label="通过">
            通过
          </Option>
          <Option value={1} label="失败">
            失败
          </Option>
          <Option value={5} label="阻塞">
            阻塞
          </Option>
          <Option value={4} label="不执行">
            不执行
          </Option>
        </Select>

        {/* <Button
          onClick={() => {
            this.jsonPatch()
          }}
        >
          jsonPatch
        </Button> */}
      </div>
    )
  }
}
