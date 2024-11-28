import React, { Component } from 'react'
import { Icon, Button, Tooltip } from 'antd'
import { CustomIcon } from '../components'
import './PriorityGroup.scss'

class ProgressGroup extends Component {
  handleAction = priority => {
    const { minder } = this.props
    minder.execCommand('Progress', priority)

    /**
     * 需要判断对应的节点是不是根节点
     *  不论是否是根节点，都要发送消息或者调用接口通知服务端记录
     *  服务端会判断当前节点是否是叶子节点，
     *    如果不是叶子节点，不会添加到执行记录中
     *    如果是叶子节点，添加到执行记录中
     */
    this.props.sendEidtProgressMessage(minder, priority)

    // console.log('props ==', this.props)
    // console.log('minder.getSelectedNodes', minder.getSelectedNodes())
    // console.log('minder priority', priority)
  }

  render() {
    const { minder, isLock } = this.props
    let disabled = minder.getSelectedNodes().length === 0
    if (isLock) disabled = true
    const btnProps = {
      type: 'link',
      disabled,
      style: { padding: 4, height: 28 },
    }
    const progressList = [
      {
        label: '移除结果',
        icon: (
          <Icon
            type="minus-circle"
            theme="filled"
            style={{ fontSize: '18px', color: 'rgba(0, 0, 0, 0.6)' }}
          />
        ),
      },
      {
        label: '失败',
        value: 1,
        icon: <CustomIcon type="fail" disabled={disabled} style={{ width: 18, height: 18 }} />,
      },
      {
        label: '通过',
        value: 9,
        icon: <CustomIcon type="checked" disabled={disabled} style={{ width: 18, height: 18 }} />,
      },
      {
        label: '阻塞',
        value: 5,
        icon: <CustomIcon type="block" disabled={disabled} style={{ width: 18, height: 18 }} />,
      },
      {
        label: '不执行',
        value: 4,
        icon: <CustomIcon type="skip" disabled={disabled} style={{ width: 18, height: 18 }} />,
      },
    ]
    return (
      <div className="nodes-actions" style={{ width: 140 }}>
        {progressList &&
          progressList.map(item => (
            <Tooltip
              key={item.value || 0}
              title={item.label}
              getPopupContainer={triggerNode => triggerNode.parentNode}
            >
              <Button {...btnProps} onClick={() => this.handleAction(item.value)}>
                {item.icon}
              </Button>
            </Tooltip>
          ))}
      </div>
    )
  }
}
export default ProgressGroup
