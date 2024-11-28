import React from 'react'
import debounce from 'lodash/debounce'
import { Tag, Select, message } from 'antd'
import request from '@/utils/axios'

const { Option } = Select

class TagSelect extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      checkedRequires: [],
      searchRequires: [],
      selectValue: undefined,
    }
    this.delaySearchRequires = debounce(this.handleSelectSearch, 500)
    // this.selectElement = React.createRef()
  }

  handleSelectSearch = value => {
    // 通过输入内容来搜索需求
    // console.log('handleSelectSearch input ==',input)
    // let value = input.splice('-')[1]
    if (value == null || value == undefined || value == '') {
      // message.warn('handleSelectSearch 为空')
      this.setState({ searchRequires: [] })
    } else {
      // 此处要发送请求找对对应的需求列表
      let url = `https://test-bd-in.test.shantaijk.cn/yx/queryReqInfoBySubject`
      let params = {
        subject: value,
      }
      request(url, {
        method: 'POST',
        body: params,
      }).then(res => {
        // console.log(' res ', res)
        // const map = new Map()
        let retArray = Array.from(Object.values(res),x=>x)
        let searchResult = retArray.map(item => {
          return {
            id: item.id,
            businessOwner: item.businessOwner,
            devOwner: item.devOwner,
            testOwner: item.testOwner,
            requireName: item.subject,
            requireId: item.serialNumber,
            requireUrl: 'https://devops.aliyun.com/projex/req/' + item.serialNumber,
          }
        })
        // 搜索到的数据去重
        let newSearchResult = []
        let requireIdList = []
        for (let i = 0; i < searchResult.length; i++) {
          if (!requireIdList.includes(searchResult[i].requireId)) {
            newSearchResult.push(searchResult[i])
            requireIdList.push(searchResult[i].requireId)
          }
        }
        let filterResult = []
        // 过滤掉已经选中的内容
        if (this.props.requirements) {
          let requirementsIds = this.props.requirements.map(item => item.requireId)
          filterResult = newSearchResult.filter(item => !requirementsIds.includes(item.requireId))
        } else {
          filterResult = newSearchResult
        }
        // console.log('filterResult value ===',filterResult)
        // console.log('newSearchResult value ===',newSearchResult)
        this.setState({ searchRequires: filterResult })
      })
    }
  }

  handleSelectChange = input => {
    //选中的element添加到已经选中的标签
    let value = input.split('_')[0]
    let checkedItem = this.state.searchRequires.filter(item => item.requireId === value)
    let copyCheckedRequires = this.props.requirements
    if (copyCheckedRequires && copyCheckedRequires.map(item => item.requireId).includes(value)) {
      message.error('当前需求已经被添加过')
    } else {
      // copyCheckedRequires.push(checkedItem[0])
      this.props.addRequirements(checkedItem[0])
      this.setState({
        searchRequires: [],
        selectValue: undefined,
      })
    }
  }

  removeElement = requireId => {
    requireId && this.props.removeRequirement(requireId)
  }

  render() {
    const { requirements } = this.props
    // console.log('requirements ===',requirements)
    return (
      <div>
        {requirements &&
          requirements.map(item => {
            return (
              <Tag closable key={item.requireId} onClose={() => this.removeElement(item.requireId)}>
                <a href={item.requireUrl} target="_blank" rel="noreferrer">
                  {item.requireId}-{item.requireName}
                </a>
              </Tag>
            )
          })}
        <Select
          // ref={this.selectElement}
          placeholder="请输入需求"
          showSearch
          value={this.state.selectValue}
          autoClearSearchValue
          onChange={value => this.handleSelectChange(value)}
          onSearch={value => this.delaySearchRequires(value)}
        >
          {this.state.searchRequires &&
            this.state.searchRequires.map(item => {
              // console.log('index value ==',index)
              return (
                <Option
                  value={item.requireId + '_' + item.requireName}
                  label={item.id}
                  key={item.requireId}
                >
                  {item.requireId}-{item.requireName}
                </Option>
              )
            })}
        </Select>
      </div>
    )
  }
}

export default TagSelect
