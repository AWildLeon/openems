{
  description = "OpenEMS Development Tooling and Build System";

  inputs = { nixpkgs.url = "github:NixOS/nixpkgs/release-25.05"; };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };

      # Fetch the oh-my-posh theme (from user's provided base)
      theme_omz = builtins.fetchurl {
        url = "https://zsh.onlh.de/theme.omp.json";
        sha256 = "1jd355hilldj4ncf0h28n70qwx43zddzn5xdxamc2y6dmlmxh79c";
      };
    in {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [ jdk11_headless gradle oh-my-posh ];

        # Create a disposable ZDOTDIR with a small .zshrc that initializes
        # oh-my-posh (with the fetched theme), fastfetch and zoxide.
        shellHook = ''
          echo "Welcome to the OpenEMS development shell!"
          eval "$(oh-my-posh init bash --config "${theme_omz}")"
        '';
      };
    };
}
