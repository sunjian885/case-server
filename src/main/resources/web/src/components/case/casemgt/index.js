/* eslint-disable */
import React from 'react';
import PropTypes from 'prop-types';
import { Breadcrumb, Row, Button, Col, message, Tooltip } from 'antd';
import './index.scss';
import request from '@/utils/axios';
import getQueryString from '@/utils/getCookies';
import moment from 'moment';
import Link from 'umi/link';
import AgileTCEditor from '../../react-mindmap-editor';

const getCookies = getQueryString.getCookie;
var saveInterval = null
/* global staffNamePY */
export default class CaseMgt extends React.Component {
  static propTypes = {
    params: PropTypes.any,
    form: PropTypes.any,
    productId: PropTypes.any,
    updateCallBack: PropTypes.any,
    activeProductObj: PropTypes.any,
  };
  constructor(props) {
    super(props);
    this.state = {
      modaltitle: '',
      visibleStatus: false,
      visible: false,
      title: '',
      caseContent: '',
      id: 0,
      productId: 0,
      recordDetail: null,
      casedetail: null,
      requirementObj: [],
    };
  }
  componentDidMount() {
    // console.log('componentDidMount this.props ==',this.props)
    const { iscore } = this.props.match.params;
    if (iscore === '3') {
      this.getContentById();
    } else {
      this.getCaseById();
    }
  }

  UNSAFE_componentWillMount() {
    // 拦截判断是否离开当前页面
    window.addEventListener('beforeunload', this.handleAutoSave);
    const { iscore } = this.props.match.params;
    if(Number(iscore) === 0){
      saveInterval = setInterval(()=>{
        this.handleAutoSave()
      },180000)
    }
  }
  componentWillUnmount() {
    // 销毁拦截判断是否离开当前页面
    window.removeEventListener('beforeunload', this.handleAutoSave);
    this.handleAutoSave();
    if (this.state.iscore === 0 && saveInterval != null) {
      clearInterval(saveInterval)
    }
  }
  ///case/getRequirement
  handleAutoSave = () => {
    // e.preventDefault();
    // e.returnValue = '内容会被存储到浏览器缓存中！';
    const { iscore } = this.props.match.params;
    const minderData = this.editorNode
      ? this.editorNode.getAllData()
      : { base: 0 };
    // 是否有ws链接断开弹窗
    const hasBreak =
      document.getElementsByClassName('ws-warning') &&
      document.getElementsByClassName('ws-warning').length > 0;
      //不用理会是否有ws断开，ws断开的情况下也要保存，保存失败会在本地保存的
    // if (Number(iscore) === 0 && minderData && !hasBreak) {
    if (Number(iscore) === 0 && minderData ) {
      // 非冒烟case才可保存
      if (Number(minderData.base) > 1) {
        // message.warn('即将离开页面，自动保存当前用例。');
        //调用子组件的保存方法
        this.editorNode.updateCase()
      }
    }

  };
  getRequirementsById = requirementIds => {
    // request(`${this.props.oeApiPrefix}/business-lines/requirements`, {
    //   method: 'GET',
    //   params: { requirementIds: requirementIds },
    // }).then(res => {
    //   this.setState({ requirementObj: res, loading: false });
    // });
  };

  getCaseById = () => {
    let url = `${this.props.doneApiPrefix}/case/getCaseInfo`;
    request(url, {
      method: 'GET',
      params: { id: this.props.match.params.caseId },
    }).then(res => {
      if (res.code == 200) {
        this.setState(
          {
            casedetail: res.data,
          },
          () => {
            this.state.casedetail.requirementId &&
              this.getRequirementsById(this.state.casedetail.requirementId);
          },
        );
      } else {
        message.error(res.msg);
        this.props.history.push('/case/caseList/1');
      }
    });
  };

  ///record/getContentById
  getContentById = () => {
    let url = `${this.props.doneApiPrefix}/record/getRecordInfo`;

    request(url, {
      method: 'GET',
      params: { id: this.props.match.params.itemid },
    }).then(res => {
      if (res.code == 200) {
        this.setState({ recordDetail: res.data });
      } else {
        message.error(res.msg);
      }
    });
  };

  //清除执行记录
  clearRecord = () => {
    const params = {
      id: this.props.match.params.itemid,
      modifier: getCookies('username'),
    };

    let url = `${this.props.doneApiPrefix}/record/clear`;
    request(url, { method: 'POST', body: params }).then(res => {
      if (res.code == 200) {
        message.success('清除执行记录成功');
        this.editorNode.setEditerData(JSON.parse(res.data.caseContent));
      } else {
        message.error(res.msg);
      }
    });
  };

  render() {
    //this.props.match.params.iscore  0:需求case  3:执行记录详情
    const { match } = this.props;
    const { iscore, caseId, itemid } = match.params;
    const userid = JSON.parse(localStorage.getItem('userinfo')).userid
    const user = getCookies('username');
    const { recordDetail, casedetail } = this.state;
    let readOnly = false;
    let progressShow = false;
    let addFactor = false;
    if (iscore === '0' || iscore === '1') {
      readOnly = false;
      progressShow = false;
      addFactor = true;
    } else {
      readOnly = true;
      progressShow = true;
      addFactor = false;
    }
    return (
      <div style={{ position: 'relative', minHeight: '80vh' }}>
        <Breadcrumb style={{ marginBottom: 8, fontSize: 12 }}>
          <Breadcrumb.Item>
            <Link to="/case/caseList/1">
              {casedetail ? '用例' : '任务'}管理
            </Link>
          </Breadcrumb.Item>
          <Breadcrumb.Item>
            {casedetail ? '用例' : '任务'}详情：
            {recordDetail ? recordDetail.title : ''}
            {casedetail ? casedetail.title : ''}
          </Breadcrumb.Item>
        </Breadcrumb>
        <div
          style={{
            padding: 12,
            background: '#fff',
          }}
        >
          {(recordDetail && (
            <Row>
              <Col span={6} className="description-case elipsis-case">
                <Tooltip
                  title={recordDetail.description}
                  placement="bottomLeft"
                >
                  {recordDetail.description}
                </Tooltip>
              </Col>
              <Col span={1}></Col>

              <Col span={2} className="font-size-12">
                通过率: {recordDetail.passRate.toFixed(2) + '%'}
              </Col>
              <Col span={2} className="font-size-12">
                {' '}
                已测: {recordDetail.passCount + '/' + recordDetail.totalCount}
              </Col>
              <Col
                span={4}
                style={{ textAlign: 'center' }}
                className="progress"
              >
                <div>
                  {(
                    <Tooltip
                      title={`通过:${recordDetail.successCount} (${(
                        (recordDetail.successCount / recordDetail.totalCount) *
                        100
                      ).toFixed(2)}%)`}
                      className="font-size-12"
                    >
                      <div
                        className="div-wrap"
                        style={{
                          width: `${(recordDetail.successCount /
                            recordDetail.totalCount) *
                            100}%`,
                          backgroundColor: '#61C663',
                        }}
                      >
                        <span></span>
                      </div>
                    </Tooltip>
                  ) || null}
                  {(recordDetail.blockCount > 0 && (
                    <Tooltip
                      title={`阻塞:${recordDetail.blockCount} (${(
                        (recordDetail.blockCount / recordDetail.totalCount) *
                        100
                      ).toFixed(2)}%)`}
                      className="font-size-12"
                    >
                      <div
                        className="div-wrap"
                        style={{
                          width: `${(recordDetail.blockCount /
                            recordDetail.totalCount) *
                            100}%`,
                          backgroundColor: '#85A1D6',
                        }}
                      >
                        <span></span>
                      </div>
                    </Tooltip>
                  )) ||
                    null}
                  {(recordDetail.bugNum > 0 && (
                    <Tooltip
                      title={`失败:${recordDetail.bugNum} (${(
                        (recordDetail.bugNum / recordDetail.totalCount) *
                        100
                      ).toFixed(2)}%)`}
                    >
                      <div
                        className="div-wrap"
                        style={{
                          width: `${(recordDetail.bugNum /
                            recordDetail.totalCount) *
                            100}%`,
                          backgroundColor: '#FF7575',
                        }}
                      >
                        <span></span>
                      </div>
                    </Tooltip>
                  )) ||
                    null}
                  {(recordDetail.totalCount - recordDetail.passCount > 0 && (
                    <Tooltip
                      title={`未执行:${recordDetail.totalCount -
                        recordDetail.passCount} (${(
                        ((recordDetail.totalCount - recordDetail.passCount) /
                          recordDetail.totalCount) *
                        100
                      ).toFixed(2)}%)`}
                    >
                      <div
                        className="div-wrap"
                        style={{
                          width: `${((recordDetail.totalCount -
                            recordDetail.passCount) /
                            recordDetail.totalCount) *
                            100}%`,
                          backgroundColor: '#EDF0FA',
                        }}
                      >
                        <span></span>
                      </div>
                    </Tooltip>
                  )) ||
                    null}
                </div>
              </Col>
              <Col span={1}></Col>
              <Col span={2} className="font-size-12">
                计划周期:
              </Col>
              <Col span={4} className="font-size-12">
                {recordDetail.expectStartTime
                  ? moment(recordDetail.expectStartTime).format('YYYY/MM/DD')
                  : null}
                -{' '}
                {recordDetail.expectEndTime
                  ? moment(recordDetail.expectEndTime).format('YYYY/MM/DD')
                  : null}
              </Col>
            </Row>
          )) ||
            null}

          {(casedetail && (
            <Row>
              <Col span={6} className="description-case elipsis-case">
                <Tooltip title={casedetail.description} placement="topLeft">
                  {casedetail.description}
                </Tooltip>
              </Col>
              <Col span={1}></Col>
              <Col span={2} className="font-size-12">
                关联需求:
              </Col>
              <Col span={14} className="font-size-12">
                {casedetail ? casedetail.requirementId : ''}
              </Col>
            </Row>
          )) ||
            null}

          <AgileTCEditor
            ref={editorNode => (this.editorNode = editorNode)}
            tags={['前置条件', '执行步骤', '预期结果']}
            iscore={iscore}
            progressShow={progressShow}
            readOnly={readOnly}
            mediaShow={!progressShow}
            editorStyle={{ height: 'calc(100vh - 100px)' }}
            toolbar={{
              image: true,
              theme: ['classic-compact', 'fresh-blue', 'fresh-green-compat'],
              template: ['default', 'right', 'fish-bone'],
              noteTemplate: '# test',
              addFactor,
            }}
            baseUrl=""
            uploadUrl="/api/file/uploadAttachment"
            // wsUrl={`http://10.30.0.2:8097`}
            // wsUrl={`http://localhost:8097`}
            wsUrl={`https://case-wss-in.test.shantaijk.cn:9000`}
            wsParam = {{ transports:['websocket','xhr-polling','jsonp-polling'], query: { caseId: caseId, recordId: itemid, user: user, userid: userid }}}
            // wsUrl={`ws://localhost:8094/api/case/${caseId}/${itemid}/${iscore}/${user}`}
            onSave={
              Number(iscore) !== 2
                ? () => {
                    message.loading('保存中......', 1);
                    this.editorNode.updateCase();
                  }
                : null
            }
          />
        </div>
      </div>
    );
  }
}
