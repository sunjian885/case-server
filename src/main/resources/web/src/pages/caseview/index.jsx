import React from 'react'
import Headers from '../../layouts/headers'
import { message, Spin, Card, Button } from 'antd'
import request from '@/utils/axios'
import './index.scss'
import AgileTCEditor from '../../components/react-mindmap-editor'

//预览case，并不能编辑，智能查看
class CaseView extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      loading: false,
      info: [],
    }
  }
  componentDidMount() {
    this.setState({ loading: true })
    const backupId = this.props.location.query.backupId
    //获取备份id对应的用例，并且赋值给minder
    request(`/backup/getBackupByid`, {
      method: 'GET',
      params: {
        id: backupId,
      },
    }).then(res => {
      this.setState({ loading: false })
      if (res.code === 200) {
        this.editorNode.setEditerData(JSON.parse(res.data.caseContent))
        // this.editorNode.setEditerData(JSON.parse(res.data.caseContent).root)
      } else {
        message.error(res.msg)
      }
    })
  }

  rollbackCase() {
    const backupId = this.props.location.query.backupId
    //回滚用例
    request(`/case/rollbackCase`, {
      method: 'POST',
      body: {
        id: backupId,
      },
    }).then(res => {
      if (res.code === 200) {
        // console.log('res.data ====',res.data)
        message.success('回滚完成')
        // this.editorNode.setEditerData(JSON.parse(res.data.caseContent).root)
        //   this.setState({ info: res.data.backupinfo });
      } else {
        message.error(res.msg)
      }
    })
  }

  render() {
    return (
      <React.Fragment>
        <Headers />
        <Spin tip="Loading..." spinning={this.state.loading}>
          <div className="historyBox">
            <div className="box_title">
              <Card bordered={false} title="历史用例查看" className="title_history">
                <Button type="primary" onClick={() => this.rollbackCase()}>
                  回滚到当前用例
                </Button>
              </Card>
            </div>
            <AgileTCEditor
              ref={editorNode => (this.editorNode = editorNode)}
              tags={['前置条件', '执行步骤', '预期结果']}
              progressShow={false}
              readOnly={true}
              mediaShow={true}
              editorStyle={{ height: 'calc(100vh - 240px)' }}
              toolbar={{
                image: true,
                theme: ['classic-compact', 'fresh-blue', 'fresh-green-compat'],
                template: ['default', 'right', 'fish-bone'],
                noteTemplate: '# test',
              }}
              uploadUrl="/api/file/uploadAttachment"
              // wsUrl="ws://localhost:8094/api/case/2227/undefined/0/user"
              type="compare"
            />
          </div>
        </Spin>
      </React.Fragment>
    )
  }
}

export default CaseView
