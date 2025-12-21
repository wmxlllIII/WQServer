package com.example.test.server.controller;

import com.example.test.common.constant.JwtClaimsConstant;
import com.example.test.common.context.BaseContext;
import com.example.test.common.properties.JwtProperties;
import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.common.utils.JwtUtil;
import com.example.test.pojo.dto.*;
import com.example.test.pojo.entity.Comment;
import com.example.test.pojo.entity.Movie;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.*;
import com.example.test.server.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/sendcode")
    @ApiOperation("用户获取验证码")
    public Result<Void> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        userService.getCode(sendCodeDTO);
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<RegisterVO> register(@RequestBody RegisterDTO registerDTO) {
        log.info("用户注册:{}", registerDTO);
        User user = userService.register(registerDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        RegisterVO registerVO = RegisterVO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .uuNumber(user.getUuNumber())
                .token(token)
                .avatarUrl(user.getAvatarUrl())
                .status("active")
                .emailVerified(false)
                .registerTime(user.getCreateAt())
                .build();


        return Result.success(registerVO);
    }


    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录:#{}", loginDTO);
        User user = userService.login(loginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .uuNumber(user.getUuNumber())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
        log.info("userLoginVO:{}", userLoginVO);

        return Result.success(userLoginVO);
    }

    @PostMapping("/autoLogin")
    @ApiOperation("用户自动登录")
    public Result<UserLoginVO> autoLogin() {
        long userId = BaseContext.getCurrentId();
        User user = userService.autoLogin(userId);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .name(user.getUsername())
                .email(user.getEmail())
                .uuNumber(user.getUuNumber())
                .avatarUrl(user.getAvatarUrl())
                .token(token)
                .build();
        log.info("userLoginVO:{}", userLoginVO);

        return Result.success(userLoginVO);
    }

    @PostMapping(value = "/avatar")
    @ApiOperation("更新用户头像")
    public Result<String> updateAvatar(@ModelAttribute AvatarUploadDTO avatarUploadDTO) {
        String avatarUrl = userService.updateAvatar(avatarUploadDTO);
//        UpdateAvatarVO updateAvatarVO = UpdateAvatarVO.builder()
//                .avatarUrl(avatarUrl)
//                .build();
//        return Result.success(UpdateAvatarVO);
        return Result.success(avatarUrl);
    }

    @PostMapping("/updateuserinfo")
    @ApiOperation("更新个人信息")
    public Result<UpdateUserInfoVO> updateUserInfo(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        userService.updateUserInfo(updateUserInfoDTO);
        UpdateUserInfoVO updateUserInfoVO = UpdateUserInfoVO.builder()
                .userName(updateUserInfoDTO.getUserName())
                .build();
        return Result.success(updateUserInfoVO);
    }

    @PostMapping(value = "/searchUser")
    @ApiOperation(value = "搜索用户")
    public Result<SearchUserVO> getUsers(@RequestBody SearchUserDTO searchUserDTO) {
        User user = userService.searchUser(searchUserDTO);

        FriendInfoVO friendInfoVO = FriendInfoVO.builder()
                .uuNumber(user.getUuNumber())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .updateAt(user.getUpdateAt())
                .build();
        SearchUserVO searchUserVO = SearchUserVO.builder()
                .FriendInfoVO(friendInfoVO)
                .isFriend(true)
                .isInBlackList(true)
                .build();
        return Result.success(searchUserVO);
    }

    @PostMapping(value = "/friend/apply")
    @ApiOperation(value = "申请好友")
    public Result<FriendApplyVO> applyForFriend(@RequestBody FriendApplyDTO friendApplyDTO) {
        String state = userService.FriendApply(friendApplyDTO);
        FriendApplyVO friendApplyVO = FriendApplyVO.builder()
                .state(state)
                .build();

        return Result.success(friendApplyVO);
    }

    @PostMapping(value = "/friend/applyResult")
    @ApiOperation(value = "处理好友请求结果")
    public Result<HandleFriendRequestVO> handleFriendApplyResult(@RequestBody HandleFriendRequestDTO friendRequestDTO) {
        HandleFriendRequestVO handleFriendRequestVO = userService.handleResponse(friendRequestDTO);
        return Result.success(handleFriendRequestVO);
    }

    @PostMapping("friend/allrequests")
    @ApiOperation(value = "获取所有好友关系")
    public Result<List<Map<String, Object>>> getAllFriendRequest() {
        List<Map<String, Object>> allFriendRequest = userService.getAllFriendRequest();
        return Result.success(allFriendRequest);
    }

    @PostMapping("friend/getAllFriends")
    @ApiOperation(value = "获取所有好友")
    public Result<List<FriendInfoVO>> getAllFriend() {
        List<FriendInfoVO> friendInfoVOList = userService.getAllFriends();

        return Result.success(friendInfoVOList);
    }

    @PostMapping("msg/send")
    @ApiOperation(value = "发消息")
    public Result<List<MsgVO>> sendMsg(@RequestBody MsgDTO msgDTO) {
        return Result.success(userService.handleMsg(msgDTO));
    }

    @PostMapping("msg/getMsg")
    @ApiOperation(value = "获取消息")
    public PageResult<MsgVO> getMsg(@RequestBody GetMsgDTO getMsgDTO) {
        return userService.getMsg(getMsgDTO);
    }

    @PostMapping("movie/movies")
    @ApiOperation(value = "获取电影列表")
    public Result<List<Movie>> getMovies() {
        List<Movie> movieList = userService.getMovies();
        return Result.success(movieList);
    }

    @PostMapping("movie/rooms")
    @ApiOperation(value = "获取房间列表")
    public Result<List<RoomVO>> getRooms() {
        List<RoomVO> rooms = userService.getRooms();
        return Result.success(rooms);
    }

    @PostMapping("movie/saveroom")
    @ApiOperation(value = "保存房间")
    public void saveRooms(@RequestBody SaveRoomDTO saveRoomDTO) {
        userService.saveRoom(saveRoomDTO);
    }

    @PostMapping("movie/removeroom")
    @ApiOperation(value = "销毁房间")
    public void removeRoom(@RequestBody RemoveRoomDTO removeRoomDTO) {
        userService.removeRoom(removeRoomDTO);
    }

    @PostMapping("message/shareroom")
    @ApiOperation(value = "分享消息")
    public Result<Integer> shareMessage(@RequestBody ShareMessageDTO shareDTO) {
        log.info("收到分享消息请求: {}", shareDTO);
        userService.saveShareMessage(shareDTO);
        return Result.success();
    }

    @PostMapping("post")
    @ApiOperation(value = "发布动态")
    public Result<PostsVO> createPost(@RequestBody PostsDTO postsDTO) {
        PostsVO postsVO = userService.publishPost(postsDTO);
        return Result.success(postsVO);
    }

    @PostMapping("/getpost")
    @ApiOperation("分页获取动态")
    public PageResult<PostsVO> getPost(@RequestBody PostsQueryDTO postsQueryDTO) {
        return userService.getPosts(postsQueryDTO);
    }

    @PostMapping("/getmypost")
    @ApiOperation("分页获取自己作品")
    public PageResult<PostsVO> getMyPost(@RequestBody PostsQueryDTO postsQueryDTO) {
        return userService.getMyPosts(postsQueryDTO);
    }

    @PostMapping("getComment")
    @ApiOperation("分页获取评论")
    public PageResult<GetCommentVO> getComment(@RequestBody GetCommentDTO commentDTO) {
        return userService.getComment(commentDTO);
    }

    @PostMapping("addComment")
    @ApiOperation("发布评论")
    public Result<Comment> addComment(@RequestBody AddCommentDTO addCommentDTO) {
        return userService.addComment(addCommentDTO);
    }

    @PostMapping("/getStsPermission")
    @ApiOperation("获取上传权限")
    public Result<StsVO> getSts() {
        StsVO sts = userService.getSts();
        return Result.success(sts);
    }
}
