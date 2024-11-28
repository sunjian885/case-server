import React from 'react'
import { Card, Col, Row, Input, Button } from 'antd'
import request from '@/utils/axios'

const { TextArea } = Input

class AIGenerate extends React.Component {
  constructor(props) {
    super(props)
    this.contentRef = React.createRef()
    this.state = {
      key: 'AI_CASE',
      activeTabKey: 'AI_CASE',
      caseContent: '暂无生成内容',
      prompts: {
        AI_CASE: '用例生成提示',
        AI_PROGRAM: '测试方案生成提示',
        AI_CODE: '测试代码生成提示',
      },
      loading: false,
    }
  }
  onTabChange = (key, type) => {
    this.setState({ [type]: key })
  }

  componentDidMount() {
    this.getAllTips()
  }

  //调用生成AI结果接口，然后将值赋予
  getAIResult = () => {
    // console.log('this.contentRef.current.value==',this.contentRef.current.state.value)
    this.setState({
      loading: true,
    })
    request('/ai/getAIResult', {
      method: 'POST',
      body: {
        aitype: 'AI',
        aikey: this.state.activeTabKey,
        content: this.contentRef.current.state.value,
      },
    }).then(res => {
      if (res.code === 200) {
        // console.log('content == ',res.data.result)
        this.setState({ caseContent: res.data.result, loading: false })
      }
    })
  }

  getAllTips = () => {
    request('/config/getAll', {
      method: 'GET',
      params: {
        type: 'AI',
      },
    }).then(res => {
      let resPrompts = {
        AI_CASE: '用例生成提示',
        AI_PROGRAM: '测试方案生成提示',
        AI_CODE: '测试代码生成提示',
      }
      if (res.code === 200) {
        for (let i = 0; i < res.data.length; i++) {
          if (res.data[i].key === 'AI_CASE') {
            resPrompts.AI_CASE = res.data[i].value
          } else if (res.data[i].key === 'AI_PROGRAM') {
            resPrompts.AI_PROGRAM = res.data[i].value
          } else if (res.data[i].key === 'AI_CODE') {
            resPrompts.AI_CODE = res.data[i].value
          }
        }
        this.setState({ prompts: resPrompts })
      }
    })
  }

  render() {
    const tabList = [
      {
        key: 'AI_CASE',
        tab: '测试用例生成',
      },
      {
        key: 'AI_PROGRAM',
        tab: '测试方案生成',
      },
      {
        key: 'AI_CODE',
        tab: '测试代码生成',
      },
    ]
    return (
      <div>
        <Card
          style={{ width: '100%', height: '100%', display: 'flex', flexFlow: 'column' }}
          tabList={tabList}
          activeTabKey={this.state.activeTabKey}
          onTabChange={key => {
            this.onTabChange(key, 'activeTabKey')
          }}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Card bordered={false}>
                <span>需求描述：</span>
                <TextArea rows={16} ref={this.contentRef} disabled={this.state.loading}></TextArea>
                <Button
                  type="primary"
                  style={{ marginTop: 12 }}
                  onClick={() => {
                    this.getAIResult()
                  }}
                  loading={this.state.loading}
                >
                  生成
                </Button>
              </Card>
            </Col>
            <Col span={12}>
              <Card bordered={false}>
                {/* <p>{this.state.prompts[this.state.activeTabKey]}</p> */}
                <div
                  dangerouslySetInnerHTML={{
                    __html: this.state.prompts[this.state.activeTabKey].replaceAll('\n', '<br/>'),
                  }}
                ></div>
              </Card>
            </Col>
          </Row>
          <Card
            bordered={true}
            title="生成结果"
            // bordered = { false }
            style={{ marginTop: 25, flex: 1 }}
          >
            {/* <TextArea value={this.state.caseContent} disabled rows={30}></TextArea> */}
            <div
              dangerouslySetInnerHTML={{ __html: this.state.caseContent.replaceAll('\n', '<br/>') }}
            ></div>
          </Card>
        </Card>
      </div>
    )
  }
}

export default AIGenerate
