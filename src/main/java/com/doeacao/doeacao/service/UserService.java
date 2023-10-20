package com.doeacao.doeacao.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.doeacao.doeacao.model.UserLogin;
import com.doeacao.doeacao.model.Usuario;
import com.doeacao.doeacao.repository.UsuarioRepository;
import com.doeacao.doeacao.security.JwtService;

@Service
public class UserService {

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public Optional<Usuario> registerUser(Usuario user) {

        if (userRepository.findByUsuario(user.getUser()).isPresent())
            return Optional.empty();

        user.setSenha(encryptPassword(user.getSenha()));

        return Optional.of(userRepository.save(user));

    }

    public Optional<Usuario> updateUser(Usuario user) {

        if(userRepository.findById(user.getId()).isPresent()) {

            Optional<Usuario> searchUser = userRepository.findByUsuario(user.getUser());

            if ( (searchUser.isPresent()) && ( searchUser.get().getId() != user.getId()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists!", null);

            user.setSenha(encryptPassword(user.getSenha()));

            return Optional.ofNullable(userRepository.save(user));

        }

        return Optional.empty();

    }

    public Optional<UserLogin> authenticateUser(Optional<UserLogin> userLogin) {

        // Gera o Objeto de autenticação
        var credenciais = new UsernamePasswordAuthenticationToken(userLogin.get().getUser(), userLogin.get().getSenha());

        // Autentica o Usuario
        org.springframework.security.core.Authentication authentication = authenticationManager.authenticate(credenciais);

        // Se a autenticação foi efetuada com sucesso
        if (authentication.isAuthenticated()) {

            // Busca os dados do usuário
            Optional<Usuario> user = userRepository.findByUsuario(userLogin.get().getUser());

            // Se o usuário foi encontrado
            if (user.isPresent()) {

                // Preenche o Objeto usuarioLogin com os dados encontrados
                userLogin.get().setId(user.get().getId());
                userLogin.get().setNome(user.get().getNome());
                userLogin.get().setFoto(user.get().getFoto());
                userLogin.get().setToken(generateToken(userLogin.get().getUser()));
                userLogin.get().setSenha("");

                // Retorna o Objeto preenchido
                return userLogin;

            }

        }

        return Optional.empty();

    }

    private String encryptPassword(String senha) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return encoder.encode(senha);

    }

    private String generateToken(String user) {
        return "Bearer " + jwtService.generateToken(user);
    }

}

