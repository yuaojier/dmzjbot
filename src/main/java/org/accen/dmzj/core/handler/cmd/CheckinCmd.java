package org.accen.dmzj.core.handler.cmd;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.accen.dmzj.core.task.GeneralTask;
import org.accen.dmzj.util.CQUtil;
import org.accen.dmzj.util.RandomUtil;
import org.accen.dmzj.web.dao.SysGroupMemberMapper;
import org.accen.dmzj.web.vo.Qmessage;
import org.accen.dmzj.web.vo.SysGroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CheckinCmd implements CmdAdapter {
	@Value("${coolq.mem.iniCoin}")
	private int iniCoin = 20;//初始金币
	@Value("${coolq.mem.coinIncr}")
	private int coinIncr = 5;//金币递增
	@Value("${coolq.mem.iniFavorability}")
	private int iniFavorability = 0;//初始好感
	@Value("${coolq.mem.favorabilityIncr}")
	private int favorabilityIncr = 1;//好感递增
	
	@Autowired
	private SysGroupMemberMapper sysGroupMember; 
	
	@Override
	public String describe() {
		return "签到或者绑定账号";
	}

	@Override
	public String example() {
		return "签到";
	}

	private final static Pattern pattern = Pattern.compile("^(个人信息|绑定|签到)$");
	
	@Override
	public GeneralTask cmdAdapt(Qmessage qmessage, String selfQnum) {
		String message = qmessage.getMessage().trim();
		Matcher matcher = pattern.matcher(message);
		if(matcher.matches()) {
			
			GeneralTask task = new GeneralTask();
			task.setSelfQnum(selfQnum);
			task.setType(qmessage.getMessageType());
			task.setTargetId(qmessage.getGroupId());
			
			List<SysGroupMember> mems = sysGroupMember.selectByTarget(qmessage.getMessageType(), qmessage.getGroupId(), qmessage.getUserId());
			if("绑定".equals(matcher.group(1))) {
				if(mems!=null&&!mems.isEmpty()) {
					task.setMessage(CQUtil.at(qmessage.getUserId())+" 您已绑定过了喵！库存金币："+mems.get(0).getCoin()+"枚，好感度："+mems.get(0).getFavorability());
				}else {
					SysGroupMember mem = new SysGroupMember();
					mem.setType(qmessage.getMessageType());
					mem.setTargetId(qmessage.getGroupId());
					mem.setUserId(qmessage.getUserId());
					mem.setCoin(iniCoin);
					mem.setCheckinCount(0);
					mem.setFavorability(iniFavorability);
					mem.setCreateTime(new Date());
					mem.setStatus(1);
					sysGroupMember.insert(mem);
					task.setMessage(CQUtil.at(qmessage.getUserId())+" 绑定成功喵~");
				}
			}else if("签到".equals(matcher.group(1))) {
				if(mems!=null&&!mems.isEmpty()) {
					
					Date lastCheckinTime = mems.get(0).getLastCheckinTime();
					if(lastCheckinTime!=null) {
						LocalDateTime now = LocalDateTime.now();
						LocalDateTime last = lastCheckinTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
						if(now.getYear()==last.getYear()
								&&now.getMonthValue()==last.getMonthValue()
								&&now.getDayOfMonth()==last.getDayOfMonth()) {
							task.setMessage(CQUtil.at(qmessage.getUserId())+" 您今天已经签到过了喵~");
						}else {
							SysGroupMember mem = mems.get(0);
							int ci = coinIncr;
							if(coinIncr<0) {
								ci = RandomUtil.randomInt(-coinIncr);
							}
							mem.setCoin(mem.getCoin()+ci);
							mem.setCheckinCount(mem.getCheckinCount()+1);
//							int fi = favorabilityIncr;
//							if(favorabilityIncr<0) {
//								fi = RandomUtil.randomInt(6);
//							}
							mem.setFavorability(mem.getFavorability()+favorabilityIncr);
							mem.setLastCheckinTime(new Date());
							sysGroupMember.updateCheckin(mem);
							task.setMessage(CQUtil.at(qmessage.getUserId())+" 签到成功喵！库存金币："+mem.getCoin()+"(+"+ci+")"+"枚，好感度："+mem.getFavorability()+"(+"+favorabilityIncr+")");
						}
					}else {
						SysGroupMember mem = mems.get(0);
						int ci = coinIncr;
						if(coinIncr<0) {
							ci = RandomUtil.randomInt(-coinIncr);
						}
						mem.setCoin(mem.getCoin()+ci);
						mem.setCheckinCount(mem.getCheckinCount()+1);
//						int fi = favorabilityIncr;
//						if(favorabilityIncr<0) {
//							fi = RandomUtil.randomInt(6);
//						}
						mem.setFavorability(mem.getFavorability()+favorabilityIncr);
						mem.setLastCheckinTime(new Date());
						sysGroupMember.updateCheckin(mem);
						task.setMessage(CQUtil.at(qmessage.getUserId())+" 签到成功喵！库存金币："+mem.getCoin()+"(+"+ci+")"+"枚，好感度："+mem.getFavorability()+"(+"+favorabilityIncr+")");
					}
					
				}else {
					task.setMessage(CQUtil.at(qmessage.getUserId())+" 您还没绑定个人信息哦，请发送[绑定]进行绑定喵~");
				}
			}else if("个人信息".equals(matcher.group(1))) {
				if(mems!=null&&!mems.isEmpty()) {
					task.setMessage(CQUtil.at(qmessage.getUserId())+" 库存金币："+mems.get(0).getCoin()+"枚，好感度："+mems.get(0).getFavorability()+"，签到次数："+mems.get(0).getCheckinCount());
				}else {
					task.setMessage(CQUtil.at(qmessage.getUserId())+" 您还没绑定个人信息哦，请发送[绑定]进行绑定喵~");
				}
			}
			return task;
		}
		return null;
	}
	
	/**
	 * 获取一名用户的金币数，如果为负数，则表示还未绑定
	 * @param type
	 * @param targetId
	 * @param userId
	 * @return
	 */
	public int getCoin(String type,String targetId,String userId) {
		List<SysGroupMember> mems = sysGroupMember.selectByTarget(type, targetId, userId);
		if(mems==null||mems.isEmpty()) {
			return -999;
		}else {
			return mems.get(0).getCoin();
		}
	}
	/**
	 * 消耗或补充金币
	 * @param type
	 * @param targetId
	 * @param userId
	 * @param diff 需要变化金币数
	 * @return 剩余金币数 如果喂绑定，则返回-999
	 */
	public int modifyCoin(String type,String targetId,String userId,int diff) {
		List<SysGroupMember> mems = sysGroupMember.selectByTarget(type, targetId, userId);
		if(mems==null||mems.isEmpty()) {
			return -999;
		}else {
			SysGroupMember mem = mems.get(0);
//			sysGroupMember.updateCoinByTarget(mem.getCoin()+diff, type, targetId, userId);
			mem.setCoin(mem.getCoin()+diff);
			sysGroupMember.updateCheckin(mem);
			return mem.getCoin();
		}
	}

	/**
	 * 消耗× 增加好感
	 * @param type
	 * @param targetId
	 * @param userId
	 * @param diff
	 * @return 执行完后好感度
	 */
	public int modifyFav(String type,String targetId,String userId,int diff) {
		List<SysGroupMember> mems = sysGroupMember.selectByTarget(type, targetId, userId);
		if(mems==null||mems.isEmpty()) {
			return -999;
		}else {
			SysGroupMember mem = mems.get(0);
			mem.setFavorability(mem.getFavorability()+diff);
			sysGroupMember.updateCheckin(mem);
//			sysGroupMember.updateFavByTarget(mem.getFavorability()+diff, type, targetId, userId);
			return mem.getFavorability();
		}
	}
	
}